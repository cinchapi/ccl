/* Generated By:JavaCC: Do not edit this line. GrammarDefaultVisitor.java Version 7.0.3 */
package com.cinchapi.ccl.generated;

public class GrammarDefaultVisitor implements GrammarVisitor{
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
  public Object visit(ASTOr node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTAnd node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTExpression node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTPage node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTOrder node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTFunction node, Object data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=2ca7aae34aa01ca7a28abe7f4102cced (do not edit this line) */
