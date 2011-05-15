#
# A simple makefile to compile the C++ and Protocol Buffers. Run ./configure
# to modify the configurable parameters
#

-include config.make

# if PREFIX has not been defined, set it to /usr/local
PREFIX ?= /usr/local

# if PROTO_PREFIX has been defined, use it to find the headers, libraries, and
# binary protoc executable.
ifdef $(PROTO_PREFIX)
	PROTOCXXFLAGS := -I$(PROTO_PREFIX)/include
	PROTOLDFLAGS := -I$(PROTO_PREFIX)/lib
	PROTOC := $(PROTO_PREFIX)/bin/protoc
else
	PROTOCXXFLAGS := `pkg-config protobuf --cflags`
	PROTOLDFLAGS := `pkg-config protobuf --libs`
	PROTOC := protoc
endif

# Command line tools
CXX := "c++"
GXX := "g++"
PERL := perl
INSTALL := install

CXXFLAGS := -g -Wall -I. $(PROTOCXXFLAGS) $(CXXFLAGS)
LDFLAGS := $(PROTOLDFLAGS) $(LDFLAGS)

EXE = resume-gen-markdown

PROTOS := $(shell find . -name "*.proto" | tr '\n' ' ')
PROTOSRCS := $(PROTOS:%.proto=%.pb.cc)
PROTOHDRS := $(PROTOS:%.proto=%.pb.h)
SRCS := $(shell find . -name "*.cc" | tr '\n' ' ') $(PROTOSRCS)
OBJS := $(SRCS:%.cc=%.o)
DEPS := $(SRCS:%.cc=%.d)

EXAMPLE_PTXT := mconbere/mconbere.ptxt
EXAMPLE_PB := $(EXAMPLE_PTXT:%.ptxt=%.pb)
EXAMPLE_MD := $(EXAMPLE_PTXT:%.ptxt=%.md)

-include $(DEPS)

.PHONY : all
all : $(EXE)

.PHONY : clean
clean :
	-rm -f $(OBJS) $(DEPS) $(PROTOSRCS) $(PROTOHDRS) $(EXE)

$(EXE) : $(OBJS)
	$(CXX) $(LDFLAGS) $^ -o $@ 

%.o : %.cc $(PROTOHDRS)
	@$(GXX) $(CXXFLAGS) -MM $< > $@
	@$(PERL) -i -pe 's|$(*F).o|$*.o|' $@
	$(CXX) $(CXXFLAGS) -c $< -o $@

# TODO: This rule is the same as the above. Without it, main.cc uses the built
# in compilation rule.
gen-markdown/main.o : gen-markdown/main.cc $(PROTOHDRS)
	@$(GXX) $(CXXFLAGS) -MM $< > $@
	@$(PERL) -i -pe 's|$(*F).o|$*.o|' $@
	$(CXX) $(CXXFLAGS) -c $< -o $@


%.pb.cc %.pb.h : %.proto
	$(PROTOC) -Iproto --cpp_out=proto proto/*.proto 

# As an example, add a rule to compile a sample resume text file into markdown
$(EXAMPLE_PB) : $(EXAMPLE_PTXT) $(PROTOS)
	$(PROTOC) --encode=com.github.mconbere.Resume -Iproto proto/*.proto < $< > $@

$(EXAMPLE_MD) : $(EXAMPLE_PB) $(EXE)
	./$(EXE) < $(EXAMPLE_PB) > $(EXAMPLE_MD)

.PHONY : install
install: $(EXE)
	mkdir -p $(PREFIX)/bin
	$(INSTALL) $(EXE) $(PREFIX)/bin/$(EXE)

protobuf-resume/src/com/github/mconbere/ResumeProto.java: $(PROTOS)
	$(PROTOC) --java_out=protobuf-resume/src proto/ResumeProto.proto
