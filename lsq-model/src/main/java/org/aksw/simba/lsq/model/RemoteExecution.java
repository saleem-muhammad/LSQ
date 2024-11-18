package org.aksw.simba.lsq.model;

import java.util.Calendar;

import org.aksw.commons.io.util.UriToPathUtils;
import org.aksw.jenax.annotation.reprogen.HashId;
import org.aksw.jenax.annotation.reprogen.Iri;
import org.aksw.jenax.annotation.reprogen.IriType;
import org.aksw.jenax.annotation.reprogen.ResourceView;
import org.aksw.jenax.annotation.reprogen.StringId;
import org.aksw.jenax.reprogen.hashid.HashIdCxt;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Resource;

@ResourceView
public interface RemoteExecution
    extends Resource
{
    static String getLogEntryId(RemoteExecution re) {
        String serviceUrl = re.getEndpointUrl();

        String serviceId = serviceUrl == null
                ? "unknown-service"
                : UriToPathUtils.resolvePath(serviceUrl).toString()
                .replace('/', '-');

        Long seqId = re.getSequenceId();
        Calendar timestamp = re.getTimestamp();
        // TODO If there is a timestamp then use it
        // Otherwise, use sourceFileName + sequenceId
        String logEntryId = serviceId + "_" + (timestamp != null
                ? timestamp.toInstant().toString() + "_" + seqId
                : seqId);

        return logEntryId;
    }

    @StringId
    default String getStringId(HashIdCxt cxt) {
        String prefix = "remoteExec";

        String logEntryId = getLogEntryId(this);
        String result = prefix + "-" + logEntryId;

        return result;
    }

    @Iri(LSQ.Terms.host)
    @HashId
    String getHost();
    RemoteExecution setHost(String host);

    @Iri(LSQ.Terms.userAgent)
    String getUserAgent();
    RemoteExecution setUserAgent(String userAgent);

    @Iri(LSQ.Terms.hostHash)
    @HashId
    String getHostHash();
    RemoteExecution setHostHash(String hostHash);

    @Iri(LSQ.Terms.atTime)
    @HashId
    Calendar getTimestamp();
    RemoteExecution setTimestamp(Calendar calendar);

    @Iri(LSQ.Terms.endpoint)
    @IriType
    @HashId
    String getEndpointUrl();
    RemoteExecution setEndpointUrl(String endpointUrl);

    @Iri(LSQ.Terms.sequenceId)
    @HashId
    Long getSequenceId();
    RemoteExecution setSequenceId(Long value);

    @Iri(LSQ.Terms.headers)
    RemoteExecutionHeaders getHeaders();
}
