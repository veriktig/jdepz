#

JDEPS = com/veriktig/tools/jdeps

all:
	cd ${JDEPS};make;make clean

clean:
	cd ${JDEPS};make clean

clobber: clean
	rm -rf jdepz.jar
