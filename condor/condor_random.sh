universe = vanilla
Initialdir = /scratch/cluster/pkar/CS388-NLP-HW3/code
Executable = /lusr/bin/bash
Arguments = /scratch/cluster/pkar/CS388-NLP-HW3/condor/task_random.sh
+Group   = "GRAD"
+Project = "INSTRUCTIONAL"
+ProjectDescription = "HW3 for CS388"
Requirements = InMastodon 
getenv = True
Log = /scratch/cluster/pkar/CS388-NLP-HW3/logs/random.condor.log
Error = /scratch/cluster/pkar/CS388-NLP-HW3/logs/random.condor.err
Output = /scratch/cluster/pkar/CS388-NLP-HW3/logs/random.condor.out
Notification = complete
Notify_user = pkar@cs.utexas.edu
Queue 1
