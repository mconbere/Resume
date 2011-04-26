Résumé Binary Protocol
======================

Author: Morgan Conbere  
Date: April 25, 2011

Purpose
-------

In a former life, I spent a very long time tweaking the exact layout of my
résumé, which was written in a very fragile LaTeX class. I enjoyed having the
precise layout control that this method provided, but since then I have not
spent as much time worrying about formatting text for printing.

Now I spend far more time thinking about how to format data for parsing. I
thought that as a method of screening future potential employers, I would
provide my résumé in a clearly defined format. Thus I present a well-defined
protocol detailing my résumé, and the requisite tools to parse such a résumé.

Components
----------

There are two primary components to this project, each in a separate directory.

* `proto/` The protocol definition. The protocol adheres to the protobuf
  specification. It should be easily parsable by any protobuf library.

* `resume-gen-markdown/` A command line tool that takes as input an encoded
  résumé buffer and outputs the text in markdown.

Proto
-----

The protocol is documented inline. A good starting point is proto/Resume.proto.

Writing binary protobuf by hand would be an unpleasant endeavor. I instead have
been taking advantage of the text format that protobuf allows. It is not well
documented, but is simple enough to understand once you see an example. Here is
a simple résumé:

    name : "Morgan A. Conbere"
    address : {
        line : "9641 Sunset Blvd."
        line : "Beverly Hills, CA 90210"
    }
    employment : {
        company : "Aperture Science, Inc."
        note : "Participated in testing"
        note : "Am still alive"
    }
    employment : {
        company : "Black Mesa"
        note : "Confidential"
    }

To convert textual protobuf into binary protobuf, you must run it through
protoc. For example:

    protoc --encode=com.github.mconbere.Resume -Iproto proto/*.proto < MorganConbere.ptxt > MorganConbere.pb

Resume-gen-markdown
-------------------

Binary résumé buffers can be turned into nicely human readable documents in one
of two ways. First, it is possible to convert a binary buffer back into the
textual format:

    protoc --decode=com.github.mconbere.Resume -Iproto proto/*.proto < MorganConbere.pb 

Another option is converting the buffer into [Markdown](http://daringfireball.net/projects/markdown/)
using the provided tool resume-gen-markdown. To build to tool, run `make`.
[Protobuf](http://code.google.com/p/protobuf/) is the only dependency. See
`configure --help` for more information about compilation options. Here is an
example of producing mardown output from a binary buffer:

    resume-gen-markdown < MorganConbere.pb > MorganConbere.md

Comments, Questions, and Bugs
-----------------------------

If you have any comments, questions, or bug reports you may contact me directly
at `mconbere@gmail.com`, or through github directly. Feel free to use this code
in any way you wish.
