public class QueryRequest {
    // linkShow = FofaAPI.getFieldsResult(domain, "link", query, email, key, "results");
    String domain;
    String email;
    String key;
    String qbase64; // 对应 query
    String fields; // 对应 link
    String detail; // 对应 results
    int page;
    int size;
    int full;
}
