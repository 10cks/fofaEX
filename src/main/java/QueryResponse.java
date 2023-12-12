import java.util.List;

class QueryResponse {
    boolean error;

    String errmsg;
    int consumed_fpoint;
    int required_fpoints;
    int size;
    int page;
    String mode;
    String query;
    List<List<String>> results;
}