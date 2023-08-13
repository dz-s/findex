# findex
Files indexer

cross-platform Java or Kotlin library which provides a service for indexing text files.
The library interface should allow for:
- specifying the indexed files and directories and 
- querying files containing a given word. 

The library should support concurrent access and react to changes in the (watched part of) filesystem. 
The library should be extensible by the tokenization algorithm (simple splitting by words/support lexers/etc.). 

#### TBD:
- some tests and a program with usage examples is advised.
- additional methods that can be useful for library users.

