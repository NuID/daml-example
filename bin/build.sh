#!/bin/sh

echo "Cleaning environment..."
rm -rf .daml target resources/public/js/example
echo "Building DAML..."
daml build
echo "Generating DAML source..."
clojure -A:codegen
echo "Compiling generated DAML source..."
boot javac target -d "target/classes"
echo "Done!"
echo "Compiling browser source..."
npx shadow-cljs release login home usdbanklogin usdbank slides
echo "Done!"
