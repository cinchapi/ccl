import java.util.List;
import java.util.ArrayList;

public class ASTRelationalExpression extends SimpleNode {
  private String key = "";
  private String operator = "";
  private List<String> values = new ArrayList<>();
  private List<String> timestamps = new ArrayList<>();

  public ASTRelationalExpression(int id) {
    super(id);
  }

  public ASTRelationalExpression(Grammar p, int id) {
    super(p, id);
  }

  /**
   * Set the key.
   * @param key the key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Set the operator.
   * @param operator operator
   */
  public void setOperator(String operator) {
    this.operator = operator;
  }

  /**
   * Add a value.
   * @param value the value
   */
  public void addValue(String value) {
    this.values.add(value);
  }

  /**
   * Set the timestamp.
   * @param timestamp
   */
  public void addTimestamp(String timestamp) {
    this.timestamps.add(timestamp);
  }

  /**
   * {@inheritDoc}
   * @see org.javacc.examples.jjtree.eg2.SimpleNode#toString()
   */
  public String toString() {
      String string = key + " " + operator;
      for (String value : values) {
            string += " " + value;
      }
      if (timestamps.size() > 0) {
          string += " at";
          for (String timestamp : timestamps) {
                string += " " + timestamp;
          } 
      }
      return string;
  }

  /** Accept the visitor. **/
  public Object jjtAccept(GrammarVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
