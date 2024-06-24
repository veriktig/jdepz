#

OUTPUT = ./classes

JAR = ../../../../jdepz.jar

MAIN_CLASS = com.veriktig.tools.jdeps.Main

CLASSFILE_DIR = ../classfile

CLASSFILE_SRC = \
	AccessFlags.java \
	AnnotationDefault_attribute.java \
	Annotation.java \
	AttributeException.java \
	Attribute.java \
	Attributes.java \
	BootstrapMethods_attribute.java \
	CharacterRangeTable_attribute.java \
	ClassFile.java \
	ClassReader.java \
	ClassTranslator.java \
	ClassWriter.java \
	Code_attribute.java \
	CompilationID_attribute.java \
	ConstantPoolException.java \
	ConstantPool.java \
	ConstantValue_attribute.java \
	DefaultAttribute.java \
	Dependencies.java \
	Dependency.java \
	Deprecated_attribute.java \
	DescriptorException.java \
	Descriptor.java \
	EnclosingMethod_attribute.java \
	Exceptions_attribute.java \
	FatalError.java \
	Field.java \
	InnerClasses_attribute.java \
	Instruction.java \
	LineNumberTable_attribute.java \
	LocalVariableTable_attribute.java \
	LocalVariableTypeTable_attribute.java \
	Method.java \
	MethodParameters_attribute.java \
	Module_attribute.java \
	ModuleHashes_attribute.java \
	ModuleMainClass_attribute.java \
	ModulePackages_attribute.java \
	ModuleResolution_attribute.java \
	ModuleTarget_attribute.java \
	NestHost_attribute.java \
	NestMembers_attribute.java \
	Opcode.java \
	package-info.java \
	PermittedSubclasses_attribute.java \
	Record_attribute.java \
	ReferenceFinder.java \
	RuntimeAnnotations_attribute.java \
	RuntimeInvisibleAnnotations_attribute.java \
	RuntimeInvisibleParameterAnnotations_attribute.java \
	RuntimeInvisibleTypeAnnotations_attribute.java \
	RuntimeParameterAnnotations_attribute.java \
	RuntimeTypeAnnotations_attribute.java \
	RuntimeVisibleAnnotations_attribute.java \
	RuntimeVisibleParameterAnnotations_attribute.java \
	RuntimeVisibleTypeAnnotations_attribute.java \
	Signature_attribute.java \
	Signature.java \
	SourceDebugExtension_attribute.java \
	SourceFile_attribute.java \
	SourceID_attribute.java \
	StackMap_attribute.java \
	StackMapTable_attribute.java \
	Synthetic_attribute.java \
	TypeAnnotation.java \
	Type.java
CLASSFILE_SRCS=$(CLASSFILE_SRC:%=$(CLASSFILE_DIR)/%)

SRCS = \
	Analyzer.java \
	Archive.java \
	ClassFileReader.java \
	DependencyFinder.java \
	DepsAnalyzer.java \
	Graph.java \
	InverseDepsAnalyzer.java \
	JdepsConfiguration.java \
	JdepsFilter.java \
	JdepsTask.java \
	JdepsWriter.java \
	Main.java \
	ModuleAnalyzer.java \
	ModuleDotGraph.java \
	ModuleExportsAnalyzer.java \
	ModuleGraphBuilder.java \
	ModuleInfoBuilder.java \
	Module.java \
	MultiReleaseException.java \
	Profile.java \
	VersionHelper.java

all: jar

compile:
	javac --release 21 -d ${OUTPUT} ${CLASSFILE_SRCS} ${SRCS}

jar: compile
	jar --create --file ${JAR} -e ${MAIN_CLASS} -C . .
	jar --update --file ${JAR} -C ${OUTPUT} .

clean:
	rm -rf ${OUTPUT}
