#

JDEPS = com/veriktig/tools/jdeps

all:
	cd ${JDEPS};make

clean:
	cd ${JDEPS};make clean

clobber: clean
	rm -rf jdepz.jar

run:
	java -jar jdepz.jar --osgi 1.2.0 ScAPI-1.2.0.jar
