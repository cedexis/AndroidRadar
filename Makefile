build-documentation-clean:
	@rm -rf doc/build

build-documentation-clean-javadoc:
	@rm -r doc/source/users/com

build-documentation: build-documentation-clean build-documentation-clean-javadoc
	javasphinx-apidoc --no-toc -o doc/source/users android-radar/src/main/
	sphinx-build -b html doc/source doc/build
