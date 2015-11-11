if(ctx._source.reportedBackHistory == null) {
    if (ctx._source.reportedBack != null) {
        ctx._source.reportedBackHistory = [ ctx._source.reportedBack ]
    } else {
        ctx._source.reportedBackHistory = []
    }
}
ctx._source.reportedBackHistory += currentStatus
ctx._source.reportedBack = currentStatus