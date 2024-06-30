#

JDEPS = com/veriktig/tools/jdeps

all:
	cd ${JDEPS};make

clean:
	cd ${JDEPS};make clean

clobber: clean
	rm -rf jdepz.jar
