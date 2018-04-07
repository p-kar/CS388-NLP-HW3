universe = vanilla
Initialdir = /scratch/cluster/pkar/CS388-NLP-HW3/code
Executable = /lusr/bin/bash
Arguments = /scratch/cluster/pkar/CS388-NLP-HW3/condor/task_length.sh
+Group   = "GRAD"
+Project = "INSTRUCTIONAL"
+ProjectDescription = "HW3 for CS388"
Requirements = InMastodon 
getenv = True
Log = /scratch/cluster/pkar/CS388-NLP-HW3/logs/length.condor.log
Error = /scratch/cluster/pkar/CS388-NLP-HW3/logs/length.condor.err
Output = /scratch/cluster/pkar/CS388-NLP-HW3/logs/length.condor.out
Notification = complete
Notify_user = pkar@cs.utexas.edu
Queue 1
