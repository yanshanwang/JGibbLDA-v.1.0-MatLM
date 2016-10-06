#MatLM: a Java Package for Matrix Reformulations of Probabilistic Language Models in Information Retrieval
##Running the package
1. Make sure you have installed java and maven.
2. Run `maven clean install` to install the depedent packages. 
3. If `BUILD SUCCESS`, you will be able to run `LdaInfoRetrieval.java` and `LdaInfoRetrievalSparse` without additional configuration. 

##Running your own data
1. Make sure the input data follows the format required by `JGibbLDA`.
2. Run package `JGibbLDA` to obtain results of LDA. (see details: http://jgibblda.sourceforge.net/)
3. Change input and output directories in `LdaInfoRetrieval.java`.

Please refer to:
Yanshan Wang. MatLM: a Matrix Reformulation of Probabilistic Language Models. Tech Report. 2016.
