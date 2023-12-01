
find ./*/src -type f -name *Controller.java | while read file; do
  echo -e "\n// `basename $file`\n// `dirname $file`\n`grep Mapping\( $file`"
done | sed 's|  ||;s|\.\/||' | bat --language java
