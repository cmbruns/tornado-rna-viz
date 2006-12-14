# Change of build version suggests a new build for bug fix, patch, or any other small reason
# Get the subversion revision number if we can
# It's possible that WIN32 installs use svnversion through cygwin.
FIND_PROGRAM (SVNVERSION svnversion)
IF (SVNVERSION-NOTFOUND)
  MESSAGE (STATUS 
    "Could not find svnversion executable. Suggest you install a
Subversion client with svnversion in path."
    )
  SET (SVN_REVISION unknown) 
ELSE (SVNVERSION-NOTFOUND)
  EXEC_PROGRAM (${SVNVERSION}
    ARGS "${PROJECT_SOURCE_DIR}"
    OUTPUT_VARIABLE SVN_REVISION)
ENDIF (SVNVERSION-NOTFOUND)
# Remove colon from build version, for easier placement in directory names
STRING(REGEX REPLACE ":" "_" SVN_REVISION ${SVN_REVISION})
