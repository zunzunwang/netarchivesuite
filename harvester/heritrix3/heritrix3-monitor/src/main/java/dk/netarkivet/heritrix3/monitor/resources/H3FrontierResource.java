package dk.netarkivet.heritrix3.monitor.resources;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.netarchivesuite.heritrix3wrapper.ScriptResult;

import com.antiaction.common.filter.Caching;
import com.antiaction.common.templateengine.TemplateBuilderFactory;

import dk.netarkivet.common.utils.Settings;
import dk.netarkivet.harvester.HarvesterSettings;
import dk.netarkivet.heritrix3.monitor.Heritrix3JobMonitor;
import dk.netarkivet.heritrix3.monitor.NASEnvironment;
import dk.netarkivet.heritrix3.monitor.NASUser;
import dk.netarkivet.heritrix3.monitor.Pagination;
import dk.netarkivet.heritrix3.monitor.ResourceAbstract;
import dk.netarkivet.heritrix3.monitor.ResourceManagerAbstract;

public class H3FrontierResource implements ResourceAbstract {

    private NASEnvironment environment;

    protected int R_FRONTIER = -1;
    
    @Override
    public void resources_init(NASEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public void resources_add(ResourceManagerAbstract resourceManager) {
        R_FRONTIER = resourceManager.resource_add(this, "/job/<numeric>/frontier/", false);
    }

    @Override
    public void resource_service(ServletContext servletContext, NASUser nas_user, HttpServletRequest req, HttpServletResponse resp, int resource_id, List<Integer> numerics, String pathInfo) throws IOException {
        if (NASEnvironment.contextPath == null) {
            NASEnvironment.contextPath = req.getContextPath();
        }
        if (NASEnvironment.servicePath == null) {
            NASEnvironment.servicePath = req.getContextPath() + req.getServletPath() + "/";
        }
        String method = req.getMethod().toUpperCase();
        if (resource_id == R_FRONTIER) {
            if ("GET".equals(method) || "POST".equals(method)) {
                frontier_list(req, resp, numerics);
            }
        }
    }

    public void frontier_list(HttpServletRequest req, HttpServletResponse resp, List<Integer> numerics) throws IOException {
        Locale locale = resp.getLocale();
        resp.setContentType("text/html; charset=UTF-8");
        ServletOutputStream out = resp.getOutputStream();
        Caching.caching_disable_headers(resp);

        TemplateBuilderFactory<MasterTemplateBuilder> masterTplBuilderFactory = TemplateBuilderFactory.getInstance(environment.templateMaster, "master.tpl", "UTF-8", MasterTemplateBuilder.class);
        MasterTemplateBuilder masterTplBuilder = masterTplBuilderFactory.getTemplateBuilder();

        StringBuilder sb = new StringBuilder();

        long lines;
        long linesPerPage = 100;
        long page = 1;
        long pages = 0;
        String q = null;

        String tmpStr;
        tmpStr = req.getParameter("page");
        if (tmpStr != null && tmpStr.length() > 0) {
            try {
                page = Long.parseLong(tmpStr);
            } catch (NumberFormatException e) {
            }
        }
        tmpStr = req.getParameter("itemsperpage");
        if (tmpStr != null && tmpStr.length() > 0) {
            try {
                linesPerPage = Long.parseLong(tmpStr);
            } catch (NumberFormatException e) {
            }
        }

        if (linesPerPage < 25) {
            linesPerPage = 25;
        }
        if (linesPerPage > 1000) {
            linesPerPage = 1000;
        }

        String initials = req.getParameter("initials");
        if (initials == null) {
            initials = "";
        }

        tmpStr = req.getParameter("q");
        if (tmpStr != null && tmpStr.length() > 0 && !tmpStr.equalsIgnoreCase(".*")) {
            q = tmpStr;
        } else {
        	q = "";
        }

        String additionalParams = "";
    	if (q.length() > 0) {
            additionalParams += "&q=" + URLEncoder.encode(q, "UTF-8");
    	}
    	if (initials.length() > 0) {
            additionalParams += "&initials=" + URLEncoder.encode(initials, "UTF-8");
    	}

        String script = environment.NAS_GROOVY_SCRIPT;

        String deleteStr = req.getParameter("delete");
        if (deleteStr != null && "1".equals(deleteStr) && initials != null && initials.length() > 0) {
            script += "\n";
            script += "\ninitials = \"" + initials + "\"";
            script += "\ndeleteFromFrontier '" + q + "'\n";
        } else {
            script += "\n";
            script += "\nlistFrontier '" + q + "', " + linesPerPage + ", " + (page - 1) + "\n";
        }

        long jobId = numerics.get(0);
        Heritrix3JobMonitor h3Job = environment.h3JobMonitorThread.getRunningH3Job(jobId);

        if (h3Job != null && h3Job.isReady()) {
            if (deleteStr != null && "1".equals(deleteStr) && (initials == null || initials.length() == 0)) {
                //sb.append("<span style=\"text-color: red;\">Initials required to delete from the frontier queue!</span><br />\n");
                sb.append("<div class=\"notify notify-red\"><span class=\"symbol icon-error\"></span> Initials required to delete from the frontier queue!</div>");
            }

            sb.append("<form class=\"form-horizontal\" action=\"?\" name=\"insert_form\" method=\"post\" enctype=\"application/x-www-form-urlencoded\" accept-charset=\"utf-8\">\n");
            sb.append("<label for=\"limit\">Lines per page:</label>");
            sb.append("<input type=\"text\" id=\"itemsperpage\" name=\"itemsperpage\" value=\"" + linesPerPage + "\" placeholder=\"return limit\">\n");
            sb.append("<label for=\"q\">Filter regex:</label>");
            sb.append("<input type=\"text\" id=\"q\" name=\"q\" value=\"" + q + "\" placeholder=\"regex\" style=\"display:inline;width:350px;\">\n");
            sb.append("<button type=\"submit\" name=\"show\" value=\"1\" class=\"btn btn-success\"><i class=\"icon-white icon-thumbs-up\"></i> Show</button>\n");
            sb.append("&nbsp;");
            sb.append("<label for=\"initials\">User initials:</label>");
            sb.append("<input type=\"text\" id=\"initials\" name=\"initials\" value=\"" + initials  + "\" placeholder=\"initials\">\n");
            sb.append("<button type=\"submit\" name=\"delete\" value=\"1\" class=\"btn btn-danger\"><i class=\"icon-white icon-trash\"></i> Delete</button>\n");
            sb.append("</form>\n");

            ScriptResult scriptResult = h3Job.h3wrapper.ExecuteShellScriptInJob(h3Job.jobResult.job.shortName, "groovy", script);
            lines = extractLinesAmount(scriptResult);

            if (lines > 0) {
                pages = (lines + linesPerPage - 1) / linesPerPage;
                if (pages == 0) {
                    pages = 1;
                }
            }

            if (page > pages) {
                page = pages;
            }

            sb.append("<div style=\"float:left;margin: 20px 0px;\">\n");
            sb.append("<span>Matching lines: ");
            sb.append(lines);
            sb.append(" URIs</span>\n");
            sb.append("</div>\n");
            sb.append(Pagination.getPagination(page, linesPerPage, pages, false, additionalParams));
            sb.append("<div style=\"clear:both;\"></div>");
            sb.append("<div>\n");
            sb.append("<pre>\n");
            //System.out.println(new String(scriptResult.response, "UTF-8"));
            if (scriptResult != null && scriptResult.script != null) {
                if (scriptResult.script.htmlOutput != null) {
                    sb.append("<fieldset><!--<legend>htmlOut</legend>-->");
                    sb.append(scriptResult.script.htmlOutput);
                    sb.append("</fieldset><br />\n");
                }
                if (scriptResult.script.rawOutput != null) {
                    sb.append("<fieldset><!--<legend>rawOut</legend>-->");
                    sb.append("<pre>");
                    sb.append(scriptResult.script.rawOutput);
                    sb.append("</pre>");
                    sb.append("</fieldset><br />\n");
                }
            }
            sb.append("</pre>\n");
            sb.append("</div>\n");
            sb.append(Pagination.getPagination(page, linesPerPage, pages, false, additionalParams));
            sb.append("</form>");
        } else {
            sb.append("Job ");
            sb.append(jobId);
            sb.append(" is not running.");
        }

        StringBuilder menuSb = masterTplBuilder.buildMenu(new StringBuilder(), h3Job);

        masterTplBuilder.insertContent("Job " + jobId + " Frontier", menuSb.toString(), environment.generateLanguageLinks(locale),
        		"Job " + jobId + " Frontier", sb.toString(),
        		"<meta http-equiv=\"refresh\" content=\""+Settings.get(HarvesterSettings.HARVEST_MONITOR_REFRESH_INTERVAL)+"\"/>\n").write(out);

        out.flush();
        out.close();
    }
    
    private Long extractLinesAmount(ScriptResult scriptResult) {
        Pattern pattern = Pattern.compile("\\d+");
        if (scriptResult != null && scriptResult.script != null) {
            try {
                if (scriptResult.script.htmlOutput != null) {
                    Matcher matcher = pattern.matcher(scriptResult.script.htmlOutput);
                    matcher.find();
                    String str = scriptResult.script.htmlOutput.substring(matcher.start(), matcher.end());
                    scriptResult.script.htmlOutput = scriptResult.script.htmlOutput.substring(matcher.end());
                    return Long.parseLong(str);
                } else {
                    if (scriptResult.script.rawOutput != null) {
                        Matcher matcher = pattern.matcher(scriptResult.script.rawOutput);
                        matcher.find();
                        String str = scriptResult.script.rawOutput.substring(matcher.start(), matcher.end());
                        scriptResult.script.rawOutput = scriptResult.script.rawOutput.substring(matcher.end());
                        return Long.parseLong(str);
                    }
                }
            }
            catch (Exception ex) {
                return 1L;
            }
        }
        return 1L;
    }

}
