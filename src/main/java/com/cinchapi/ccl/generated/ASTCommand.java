/*
 * Copyright (c) 2013-2017 Cinchapi Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cinchapi.ccl.generated;

import com.cinchapi.ccl.grammar.command.CommandSymbol;

/**
 * The {@code ASTCommand} class represents a specific type of abstract syntax tree (AST)
 * node for command expressions in the CCL (Concourse Command Language).
 *
 * <p>This class extends {@code SimpleNode} and adds functionality to handle
 * {@link CommandSymbol} objects, which represent the command associated with
 * the AST node.
 */
public class ASTCommand extends SimpleNode {
  private CommandSymbol command;

  /**
   * Constructs an {@code ASTCommand} with the given node ID.
   *
   * @param id the unique identifier for this AST node
   */
  public ASTCommand(int id) {
    super(id);
  }

  /**
   * Constructs an {@code ASTCommand} with the specified grammar and node ID.
   *
   * @param p  the grammar parser that creates this node
   * @param id the unique identifier for this AST node
   */
  public ASTCommand(Grammar p, int id) {
    super(p, id);
  }

  /**
   * Sets the command associated with this AST node.
   *
   * @param command the {@link CommandSymbol} representing the command
   */
  public void command(CommandSymbol command) {
    this.command = command;
  }

  /**
   * Retrieves the command associated with this AST node.
   *
   * @return the {@link CommandSymbol} representing the command
   */
  public CommandSymbol command() {
    return command;
  }

  /**
   * Accepts a visitor to perform an operation on this AST node.
   *
   * <p>This method is part of the visitor pattern implementation.
   *
   * @param visitor the {@link GrammarVisitor} to process this node
   * @param data    additional data passed to the visitor
   * @return the result of the visitor's operation
   */
  @Override
  public Object jjtAccept(GrammarVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
