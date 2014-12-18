GraMi
=====

GraMi is a novel framework for frequent subgraph mining in a single large  graph, GraMi outperforms existing techniques by 2 orders of magnitudes. GraMi  supports finding frequent subgraphs as well as frequent patterns, Compared to subgraphs, patterns offer a more powerful version of matching that captures  transitive interactions between graph nodes (like friend of a friend) which are very common in modern applications. Also, GraMi supports user-defined  structural and semantic constraints over the results, as well as approximate results.

For more details, check our paper: Mohammed Elseidy, Ehab Abdelhamid, Spiros Skiadopoulos, and Panos Kalnis. "GRAMI: Frequent Subgraph and Pattern Mining in a Single Large Graph. PVLDB, 7(7):517-528, 2014."

CONTENTS:
=====

    README ...................  This file
    LICENSE.txt ..............  License file (Open Source)
    build ....................  build GraMi java binary files
    grami ....................  script to run GraMi
    Datasets/ ................  Example graphs
    GRAMI_*/ .................  Directory containing GraMi sources


REQUIREMENTS:
=====

Java JRE v1.6.0 or later

INSTALLATION:
=====

    - Uncompress grami using any compression tool
    - Build Java binaries using the "build" script file
    - Run GraMi using "grami" script

EXAMPLES:
=====

    1- Show GraMi breif help: "./grami -h"
    2- Find frequent subgraphs in the "mico" undirected graph, with minimum frequency = 14000: "./grami -f mico.lg -s 14000 -t 0 -p 0"
    3- Find frequent subgraphs in the "mico" undirected graph, with minimum frequency = 14000 and approximation: "./grami -f mico.lg -s 9340 -t 0 -p 0 -approxA 0.0002 -approxB=0"
    4- Find frequent patterns in the "citeseer" directed graph, with minimum frequency = 160 and maximum distance bound (edge weight) = 200: "./grami -f citeseer.lg -s 160 -t 1 -p 1 -d 200"

Contributors
=====

    @Ehab-abdelhamid
    @ElSeidy


Grami On Amazon AWS    
=====
http://cloud.kaust.edu.sa/Pages/Grami.aspx
