#
# Downloads and builds our custom freerdp install package
#

REPOSITORY = https://github.com/chrislong/FreeRDP.git
CMAKE_FLAGS = \
	-D CMAKE_BUILD_TYPE=RelWithDebInfo \
	-D CPACK_BINARY_DEB=ON \
	-D CPACK_BINARY_STGZ=OFF \
	-D CPACK_BINARY_TGZ=OFF \
	-D CPACK_BINARY_TZ=OFF \
	-D CPACK_SOURCE_TBZ2=OFF \
	-D CPACK_SOURCE_TGZ=OFF \
	-D CPACK_SOURCE_TXZ=OFF \
	-D CPACK_SOURCE_TZ=OFF \
	-D WITH_SWSCALE=ON

.PHONY: default package clean

default: package

source build:
	mkdir -p $@

package: build/freerdp.deb

build/freerdp.deb: build/Makefile
	$(MAKE) -C $(@D) package
	rm -f $@
	cd $(@D) && ln -s freerdp-*-Linux-x86_64.deb $(notdir $@)

build/Makefile: source/CMakeLists.txt | build
	cd $(@D) && cmake -G 'Unix Makefiles' $(CMAKE_FLAGS) ../source
	touch $@

# can be any file in the repo
source/CMakeLists.txt: | source
	git clone --depth 1 $(REPOSITORY) source
	touch $@

clean:
	rm -rf build source
