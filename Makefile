#
# A simple makefile to compile the C++ and Protocol Buffers. Run ./configure
# to modify the configurable parameters
#

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

CXXFLAGS := -g -Wall -I. $(PROTOCXXFLAGS) $(CXXFLAGS)
LDFLAGS := $(PROTOLDFLAGS) $(LDFLAGS)

EXE = resume-gen-markdown/main

PROTOS := $(shell find . -name "*.proto" | tr '\n' ' ')
PROTOSRCS := $(PROTOS:%.proto=%.pb.cc)
PROTOHDRS := $(PROTOS:%.proto=%.pb.h)
SRCS := $(shell find . -name "*.cc" | tr '\n' ' ') $(PROTOSRCS)
OBJS := $(SRCS:%.cc=%.o)
DEPS := $(SRCS:%.cc=%.d)

-include $(DEPS)

.PHONY : all
all : $(EXE)

.PHONY : clean
clean :
	-rm -f $(OBJS) $(DEPS) $(PROTOSRCS) $(PROTOHDRS) $(EXE)

$(EXE) : $(OBJS)
	$(CXX) $(LDFLAGS) $+ -o $@ 

%.o : %.cc $(PROTOHDRS)
	@$(GXX) $(CXXFLAGS) -MM $< > $@
	@$(PERL) -i -pe 's|$(*F).o|$*.o|' $@
	$(CXX) $(CXXFLAGS) -c $< -o $@

resume-gen-markdown/main.o : resume-gen-markdown/main.cc $(PROTOHDRS)
	@$(GXX) $(CXXFLAGS) -MM $< > $@
	@$(PERL) -i -pe 's|$(*F).o|$*.o|' $@
	$(CXX) $(CXXFLAGS) -c $< -o $@


%.pb.cc %.pb.h : %.proto
	$(PROTOC) -Iproto --cpp_out=proto proto/*.proto 
