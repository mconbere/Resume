//
// main.cc -- Executable entry point for the application resume-gen-markdown
//

#include "proto/Resume.pb.h"

#include <vector>
#include <fstream>
#include <iostream>
#include <sstream>

using namespace std;
using namespace com::github::mconbere;
using namespace google::protobuf;

void generateMarkdown(Resume &resume);
string dateString(const Date &date);
string dateRangeString(const DateRange &range);

int main(int argc, char **argv) {
    Resume resume;
    bool parsed;
    if (argc == 2) {
        ifstream input(argv[1]);
        parsed = resume.ParseFromIstream(&input);
    } else {
        parsed = resume.ParseFromFileDescriptor(fileno(stdin));
    }
    
    if (!parsed) {
        cerr << "Failed to parse input. Error in section: " << resume.InitializationErrorString() << endl;
        return EXIT_FAILURE;
    }
    
    generateMarkdown(resume);
    
    return EXIT_SUCCESS;
}

// Resume constants
static const string EducationTitle = "Education";
static const string EmploymentTitle = "Employment";
static const string ProjectsTitle = "Projects";
static const string PublicationsTitle = "Publications";
static const string CurrentTime = "Present";
static const string ListEnder = "and";

void generateMarkdown(Resume &resume) {
    // Markdown constants
    static const string BreakLine = "  ";
    static const string Bold = "__";
    static const string Code = "`";
    static const string Bullet = "* ";
    
    cout << resume.name() << endl;
    cout << string(resume.name().length(), '=') << endl;
    cout << endl;
    
    if (resume.has_phone_number()) cout << resume.phone_number() << BreakLine << endl;
    if (resume.has_email_address()) cout << Code << resume.email_address() << Code << BreakLine << endl;
    
    for (int i = 0; i < resume.address().line_size(); i++) {
        cout << resume.address().line(i);
        if (i + 1 != resume.address().line_size()) cout << ", ";
    }
    cout << BreakLine << endl;
    
    if (resume.has_phone_number() || resume.has_email_address() || resume.has_address()) cout << endl;
    
    if (resume.has_objective()) {
        cout << resume.objective().title() << endl;
        cout << string(resume.objective().title().length(), '-') << endl;
        cout << resume.objective().contents() << endl;
    }
    
    if (resume.education_size() > 0) {
        cout << EducationTitle << endl;
        cout << string(EducationTitle.length(), '-') << endl;
        cout << endl;
        
        for (int i = 0; i < resume.education_size(); i++) {
            const Education &education = resume.education(i);
            cout << Bold << education.institute() << Bold << BreakLine << endl;
            if (education.has_degree()) cout << education.degree() << BreakLine << endl;
            if (education.has_date_range()) cout << dateRangeString(education.date_range()) << BreakLine << endl;
            
            cout << endl;
            for (int j = 0; j < education.note_size(); j++) {
                cout << Bullet << education.note(j) << endl;
            }
            cout << endl;
        }
    }
    
    if (resume.employment_size() > 0) {
        cout << EmploymentTitle << endl;
        cout << string(EducationTitle.length(), '-') << endl;
        cout << endl;
        
        for (int i = 0; i < resume.employment_size(); i++) {
            const Employment &employment = resume.employment(i);
            cout << Bold << employment.company() << Bold << BreakLine << endl;
            if (employment.has_title()) cout << employment.title() << BreakLine << endl;
            if (employment.has_website()) cout << Code << employment.website() << Code << BreakLine << endl;
            if (employment.has_date_range()) cout << dateRangeString(employment.date_range()) << BreakLine << endl;

            cout << endl;
            for (int j = 0; j < employment.note_size(); j++) {
                cout << Bullet << employment.note(j) << endl;
            }
            cout << endl;
        }
    }
    
    if (resume.project_size() > 0) {
        cout << ProjectsTitle << endl;
        cout << string(ProjectsTitle.length(), '-') << endl;
        cout << endl;
        
        for (int i = 0; i < resume.project_size(); i++) {
            const Project &project = resume.project(i);
            cout << Bold << project.title() << Bold << BreakLine << endl;
            if (project.has_role()) cout << project.role() << BreakLine << endl;
            if (project.has_website()) cout << Code << project.website() << Code << BreakLine << endl;
            if (project.has_date_range()) cout << dateRangeString(project.date_range()) << BreakLine << endl;
            
            cout << endl;
            for (int j = 0; j < project.note_size(); j++) {
                cout << Bullet << project.note(j) << endl;
            }
            cout << endl;
        }
    }
    
    if (resume.publication_size() > 0 ) {
        cout << PublicationsTitle << endl;
        cout << string(PublicationsTitle.length(), '-') << endl;
        cout << endl;
        
        for (int i = 0; i < resume.publication_size(); i++) {
            const Publication &publication = resume.publication(i);
            cout << Bold << publication.title() << Bold << BreakLine << endl;
            
            for (int j = 0; j < publication.author_size(); j++) {
                cout << publication.author(j);
                if (j + 2 == publication.author_size()) cout << " " << ListEnder << " ";
                else if (j + 1 != publication.author_size()) cout << ", ";
            }
            cout << BreakLine << endl;
            if (publication.has_date()) cout << dateString(publication.date()) << BreakLine << endl;

            if (publication.has_url()) cout << Code << publication.url() << Code << BreakLine << endl;

            cout << endl;
            for (int j = 0; j < publication.note_size(); j++) {
                cout << Bullet << publication.note(j) << endl;
            }
            cout << endl;
        }
    }
}

string dateString(const Date &date) {
    stringstream result;

    if (date.has_month()) {
        string month;
        switch (date.month()) {
            case Date::JANUARY:
                month = "January";
                break;
            case Date::FEBRUARY:
                month = "February";
                break;
            case Date::MARCH:
                month = "March";
                break;
            case Date::APRIL:
                month = "April";
                break;
            case Date::MAY:
                month = "May";
                break;
            case Date::JUNE:
                month = "June";
                break;
            case Date::JULY:
                month = "July";
                break;
            case Date::AUGUST:
                month = "August";
                break;
            case Date::SEPTEMBER:
                month = "September";
                break;
            case Date::OCTOBER:
                month = "October";
                break;
            case Date::NOVEMBER:
                month = "November";
                break;
            case Date::DECEMBER:
                month = "December";
                break;
        }
        result << month;
        
        if (date.has_day()) {
            result << " " << date.day();
        }
        
        if (date.has_year()) result << ", ";
    }
    
    if (date.has_year()) {
        result << date.year();
    }
    
    return result.str();
}

string dateRangeString(const DateRange &range) {
    string result;
    if (range.has_began()) result += dateString(range.began()) + " \u2013 ";
    if (!range.has_ended()) result += " " + CurrentTime;
    else result += dateString(range.ended());
    return result;
}
