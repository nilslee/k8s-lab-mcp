package com.nilslee.mcp.service.gitops;

import org.springframework.stereotype.Service;

@Service
public class JenkinsCIGitOpsService {
//  Get jobs
//  http://jenkins.k8s.lab:8080/api/json?tree=jobs[name,url,builds[number,result,duration,url]]
//

//  Get last build logs
//  (documentation) http://jenkins.k8s.lab:8080/job/mcp-server/lastBuild/api/
//  Use multipart/form-data?? check if large amounts of data are chunked.
//  http://jenkins.k8s.lab:8080/job/mcp-server/lastBuild/logText/progressiveText?start=0
}
