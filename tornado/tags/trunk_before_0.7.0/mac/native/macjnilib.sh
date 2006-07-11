for f in *.jnilib; do
  install_name_tool -id @executable_path/../Resources/Java/$f $f;
  for ff in *.jnilib; do
    install_name_tool -change $ff @executable_path/../Resources/Java/$ff $f;
  done;
done;
