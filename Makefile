#

JDEPS = com/sun/tools/jdeps

all:
	cd ${JDEPS};make

clean:
	cd ${JDEPS};make clean

clobber: clean
	rm -rf *.jar
