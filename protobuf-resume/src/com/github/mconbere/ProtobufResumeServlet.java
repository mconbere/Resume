package com.github.mconbere;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import javax.servlet.http.*;

import com.github.mconbere.ResumeProto;
import com.google.protobuf.TextFormat;

@SuppressWarnings("serial")
public class ProtobufResumeServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)	throws IOException {
		String urlToFetchEncoded = req.getParameter("url");
		
		if (urlToFetchEncoded != null) {
			URL url = new URL(URLDecoder.decode(urlToFetchEncoded, "UTF-8"));
			URLConnection connection = url.openConnection();
			String type = connection.getHeaderField("content-type");
			String[] array = type.split("=");
			String encoding = null;
			if (array.length > 0) {
				encoding = array[array.length - 1];
			}
			doRequest(connection.getInputStream(), encoding, resp);
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		doRequest(req.getInputStream(), req.getCharacterEncoding(), resp);
	}
	
	public void doRequest(InputStream stream, String encoding, HttpServletResponse resp) throws IOException {
		// copy the body of the request into a buffer, since we need to try to read it twice, once
		// as a binary encoded buffer, and once as the text format.
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		// TODO: read larger chunks for efficiency
		int len = 0;
		byte[] buffer = new byte[2048];
		while ((len = stream.read(buffer)) != -1) {
			bytes.write(buffer, 0, len);
		}
		
		ResumeProto.Resume resume = null;
		try {
			// Try converting the binary file to a resume
			resume = ResumeProto.Resume.parseFrom(bytes.toByteArray());
		} catch (IOException e) {
			// If this fails, try converting the plain text file to a resume
			ResumeProto.Resume.Builder builder = ResumeProto.Resume.newBuilder();
			TextFormat.merge(bytes.toString(encoding), builder);
			resume = builder.build();
		}
		String markdown = convertToMarkdown(resume);
		
		resp.setContentType("text/plain; charset=UTF-8");
		resp.getWriter().print(markdown);
	}
	
	final String EducationTitle = "Education";
	final String EmploymentTitle = "Employment";
	final String ProjectsTitle = "Projects";
	final String PublicationsTitle = "Publications";
	final String CurrentTime = "Present";
	final String ListEnder = "and";
	
	final String BreakLine = "  ";
	final String Bold = "__";
	final String Code = "`";
	final String Bullet = "* ";
	
	String convertToMarkdown(ResumeProto.Resume resume) {
		String out = "";
		
		if (resume.hasName()) {
			out += resume.getName() + "\n";
			out += repeat("=", resume.getName().length()) + "\n";
			out += "\n";
		}
		
		if (resume.hasPhoneNumber()) {
			out += resume.getPhoneNumber() + BreakLine + "\n";
		}
		if (resume.hasEmailAddress()) {
			out += mailto(resume.getEmailAddress()) + BreakLine + "\n";
		}
		for (int i = 0; i < resume.getAddress().getLineCount(); i++) {
			out += resume.getAddress().getLine(i);
			if (i + 1 != resume.getAddress().getLineCount()) out += ", ";
		}
		out += BreakLine + "\n";
		
		if (resume.hasPhoneNumber() || resume.hasEmailAddress() || resume.hasAddress()) {
			out += "\n";
		}
		
		if (resume.hasObjective()) {
			out += resume.getObjective().getTitle() + "\n";
			out += repeat("-", resume.getObjective().getTitle().length()) + "\n";
			out += resume.getObjective().getContents() + "\n";
			out += "\n";
		}
		
		if (resume.getEducationCount() > 0) {
			out += EducationTitle + "\n";
			out += repeat("-", EducationTitle.length()) + "\n";
			out += "\n";
			
			for (int i = 0; i < resume.getEducationCount(); i++) {
				ResumeProto.Resume.Education education = resume.getEducation(i);
				out += Bold + education.getInstitute() + Bold + BreakLine + "\n";
				if (education.hasDegree()) out += education.getDegree() + BreakLine + "\n";
				if (education.hasDateRange()) out += dateRange(education.getDateRange()) + BreakLine + "\n";
				
				out += "\n";
				for (int j = 0; j < education.getNoteCount(); j++) {
					out += Bullet + education.getNote(j) + "\n";
				}
				out += "\n";
			}
		}
		
	    if (resume.getEmploymentCount() > 0) {
	        out += EmploymentTitle + "\n";
	        out += repeat("-", EducationTitle.length()) + "\n";
	        out += "\n";
	        
	        for (int i = 0; i < resume.getEmploymentCount(); i++) {
	            ResumeProto.Resume.Employment employment = resume.getEmployment(i);
	            out += Bold + employment.getCompany() + Bold + BreakLine + "\n";
	            if (employment.hasTitle()) out += employment.getTitle() + BreakLine + "\n";
	            if (employment.hasDateRange()) out += dateRange(employment.getDateRange()) + BreakLine + "\n";
	            if (employment.hasWebsite()) out += url(employment.getWebsite()) + BreakLine + "\n";

	            out += "\n";
	            for (int j = 0; j < employment.getNoteCount(); j++) {
	                out += Bullet + employment.getNote(j) + "\n";
	            }
	            out += "\n";
	        }
	    }
	    
	    if (resume.getProjectCount() > 0) {
	        out += ProjectsTitle + "\n";
	        out += repeat("-", ProjectsTitle.length()) + "\n";
	        out += "\n";
	        
	        for (int i = 0; i < resume.getProjectCount(); i++) {
	            ResumeProto.Resume.Project project = resume.getProject(i);
	            out += Bold + project.getTitle() + Bold + BreakLine + "\n";
	            if (project.hasRole()) out += project.getRole() + BreakLine + "\n";
	            if (project.hasDateRange()) out += dateRange(project.getDateRange()) + BreakLine + "\n";
	            if (project.hasWebsite()) out += url(project.getWebsite()) + BreakLine + "\n";
	            
	            out += "\n";
	            for (int j = 0; j < project.getNoteCount(); j++) {
	                out += Bullet + project.getNote(j) + "\n";
	            }
	            out += "\n";
	        }
	    }
	    
	    if (resume.getPublicationCount() > 0 ) {
	        out += PublicationsTitle + "\n";
	        out += repeat("-", PublicationsTitle.length()) + "\n";
	        out += "\n";
	        
	        for (int i = 0; i < resume.getPublicationCount(); i++) {
	            ResumeProto.Resume.Publication publication = resume.getPublication(i);
	            out += Bold + publication.getTitle() + Bold + BreakLine + "\n";
	            
	            for (int j = 0; j < publication.getAuthorCount(); j++) {
	                out += publication.getAuthor(j);
	                if (j + 2 == publication.getAuthorCount()) out += " " + ListEnder + " ";
	                else if (j + 1 != publication.getAuthorCount()) out += ", ";
	            }
	            out += BreakLine + "\n";

	            if (publication.hasDate()) out += date(publication.getDate()) + BreakLine + "\n";
	            if (publication.hasUrl()) {
	            	out += url(publication.getUrl()) + BreakLine + "\n";
	            }

	            out += "\n";
	            for (int j = 0; j < publication.getNoteCount(); j++) {
	                out += Bullet + publication.getNote(j) + "\n";
	            }
	            out += "\n";
	        }
	    }
	    
		return out;
	}
	
	String repeat(String s, int n) {
		String r = "";
		for (int i = 0; i < n; i++) {
			r += s;
		}
		return r;
	}
	
	String date(ResumeProto.Date date) {
	    String result = "";

	    if (date.hasMonth()) {
	        String month = "";
	        switch (date.getMonth()) {
	            case JANUARY:
	                month = "January";
	                break;
	            case FEBRUARY:
	                month = "February";
	                break;
	            case MARCH:
	                month = "March";
	                break;
	            case APRIL:
	                month = "April";
	                break;
	            case MAY:
	                month = "May";
	                break;
	            case JUNE:
	                month = "June";
	                break;
	            case JULY:
	                month = "July";
	                break;
	            case AUGUST:
	                month = "August";
	                break;
	            case SEPTEMBER:
	                month = "September";
	                break;
	            case OCTOBER:
	                month = "October";
	                break;
	            case NOVEMBER:
	                month = "November";
	                break;
	            case DECEMBER:
	                month = "December";
	                break;
	        }
	        result += month;
	        
	        if (date.hasDay()) {
	            result += " " + date.getDay();
	        }
	        
	        if (date.hasYear()) result += ", ";
	    }
	    
	    if (date.hasYear()) {
	        result += "" + date.getYear();
	    }
	    
	    return result;
	}
	
	String dateRange(ResumeProto.DateRange range) {
		String result = "";
		if (range.hasBegan()) result += date(range.getBegan()) + " \u2013 ";
		if (!range.hasEnded()) result += " " + CurrentTime;
		else result += date(range.getEnded());
		return result;
	}
	
	String mailto(String email) {
		return "[" + Code + email + Code + "](mailto:" + email + ")"; 
	}
	
	String url(String in) {
		return "[" + Code + in + Code + "](" + in + ")";
	}
}
