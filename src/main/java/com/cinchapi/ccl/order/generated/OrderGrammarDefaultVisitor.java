/* Generated By:JavaCC: Do not edit this line. OrderGrammarDefaultVisitor.java Version 7.0.3 */
package com.cinchapi.ccl.order.generated;

public class OrderGrammarDefaultVisitor implements OrderGrammarVisitor{
  public Object defaultVisit(SimpleNode node, Object data){
    node.childrenAccept(this, data);
    return data;
  }
  public Object visit(SimpleNode node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTStart node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTOrder node, Object data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=f119882e5d51cf40357fb9ca54ed3eec (do not edit this line) */