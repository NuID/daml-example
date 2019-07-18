#!/bin/sh

# Would be better as a boot task
echo "Cleaning environment..."
rm -rf .daml target
echo "Building DAML..."
daml build
echo "Generating sources..."
clojure -A:codegen
echo "Compiling generated sources..."
boot javac target -d "target/classes"
echo "Done!"
