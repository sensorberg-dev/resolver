if(ctx._source.reportedBackHistory == null) {
    ctx._source.reportedBackHistory = [ctx._source.reportedBack];
}
ctx._source.reportedBackHistory += currentStatus;
ctx._source.reportedBack = currentStatus;                                      