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
package com.cinchapi.ccl;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.cinchapi.ccl.syntax.ConjunctionTree;
import com.cinchapi.ccl.syntax.ExpressionTree;
import com.cinchapi.ccl.syntax.FunctionTree;
import com.cinchapi.ccl.syntax.OrderTree;
import com.cinchapi.ccl.syntax.PageTree;

import org.junit.Assert;
import org.junit.Test;

import com.cinchapi.ccl.grammar.ConjunctionSymbol;
import com.cinchapi.ccl.grammar.ExpressionSymbol;
import com.cinchapi.ccl.grammar.OperatorSymbol;
import com.cinchapi.ccl.grammar.ParenthesisSymbol;
import com.cinchapi.ccl.grammar.PostfixNotationSymbol;
import com.cinchapi.ccl.grammar.ValueSymbol;
import com.cinchapi.ccl.grammar.KeySymbol;
import com.cinchapi.ccl.grammar.Symbol;
import com.cinchapi.ccl.syntax.AbstractSyntaxTree;
import com.cinchapi.ccl.syntax.CommandTree;
import com.cinchapi.ccl.syntax.Visitor;
import com.cinchapi.common.reflect.Reflection;
import com.cinchapi.concourse.Tag;
import com.cinchapi.concourse.Timestamp;
import com.cinchapi.concourse.lang.Criteria;
import com.cinchapi.concourse.thrift.Operator;
import com.cinchapi.concourse.time.Time;
import com.cinchapi.concourse.util.Random;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Unit tests for {@link Parser} functionality.
 * 
 * @author Jeff Nelson
 * @deprecated Replaced by {@link CompilerTest}
 */
@Deprecated
public abstract class ParserTest {

    @Test
    public void testAbstractSyntaxTreeGenerationAndGroupOr() {
        doTestAbstractSyntaxTreeGeneration(
                "graduation_rate > 90 and (percent_undergrad_black >= 5 or total_cost_out_state > 50000)");
    }

    @Test
    public void testAbstractSyntaxTreeGenerationGroupAndOrGroupAnd() {
        doTestAbstractSyntaxTreeGeneration(
                "(graduation_rate > 90 and yield_min = 20) or (percent_undergrad_black >= 5 and total_cost_out_state > 50000)");
    }

    @Test
    public void testAbstractSyntaxTreeGenerationGroupOrAndGroupOr() {
        doTestAbstractSyntaxTreeGeneration(
                "(graduation_rate > 90 or yield_min = 20) and (percent_undergrad_black >= 5 or total_cost_out_state > 50000)");
    }

    @Test
    public void testAbstractSyntaxTreeGenerationOrGroupAnd() {
        doTestAbstractSyntaxTreeGeneration(
                "graduation_rate > 90 or (percent_undergrad_black >= 5 and total_cost_out_state > 50000)");
    }

    @Test
    public void testAbstractSyntaxTreeGenerationSimpleAndOr() {
        doTestAbstractSyntaxTreeGeneration(
                "graduation_rate > 90 AND percent_undergrad_black >= 5 OR total_cost_out_of_state > 50000");
    }

    @Test
    public void testAbstractSyntaxTreeGenerationSimpleOrAnd() {
        doTestAbstractSyntaxTreeGeneration(
                "graduation_rate > 90 OR percent_undergrad_black >= 5 AND total_cost_out_of_state > 50000");
    }

    @Test
    public void testGroupAnd() {
        String key0 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator0 = com.cinchapi.concourse.thrift.Operator.EQUALS;
        Object value0 = Random.getObject();
        String key1 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator1 = com.cinchapi.concourse.thrift.Operator.GREATER_THAN;
        Object value1 = Random.getObject();
        Criteria criteria = Criteria.where().key(key0).operator(operator0)
                .value(value0).and().key(key1).operator(operator1).value(value1)
                .build();
        List<Symbol> symbols = Parsing
                .groupExpressions(Reflection.call(criteria, "symbols"));
        ExpressionSymbol exp0 = (ExpressionSymbol) symbols.get(0);
        ConjunctionSymbol sym = (ConjunctionSymbol) symbols.get(1);
        ExpressionSymbol exp1 = (ExpressionSymbol) symbols.get(2);
        Assert.assertEquals(3, symbols.size());
        Assert.assertEquals(exp0.raw().key(), key0);
        Assert.assertEquals(exp0.raw().operator(), operator0);
        Assert.assertEquals(exp0.values().get(0).value(), value0);
        Assert.assertEquals(sym, ConjunctionSymbol.AND);
        Assert.assertEquals(exp1.raw().key(), key1);
        Assert.assertEquals(exp1.raw().operator(), operator1);
        Assert.assertEquals(exp1.values().get(0).value(), value1);
    }

    @Test
    public void testGroupOr() {
        String key0 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator0 = com.cinchapi.concourse.thrift.Operator.EQUALS;
        Object value0 = Random.getObject();
        String key1 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator1 = com.cinchapi.concourse.thrift.Operator.GREATER_THAN;
        Object value1 = Random.getObject();
        Criteria criteria = Criteria.where().key(key0).operator(operator0)
                .value(value0).or().key(key1).operator(operator1).value(value1)
                .build();
        List<Symbol> symbols = Parsing
                .groupExpressions(Reflection.call(criteria, "symbols"));
        ExpressionSymbol exp0 = (ExpressionSymbol) symbols.get(0);
        ConjunctionSymbol sym = (ConjunctionSymbol) symbols.get(1);
        ExpressionSymbol exp1 = (ExpressionSymbol) symbols.get(2);
        Assert.assertEquals(3, symbols.size());
        Assert.assertEquals(exp0.raw().key(), key0);
        Assert.assertEquals(exp0.raw().operator(), operator0);
        Assert.assertEquals(exp0.values().get(0).value(), value0);
        Assert.assertEquals(sym, ConjunctionSymbol.OR);
        Assert.assertEquals(exp1.raw().key(), key1);
        Assert.assertEquals(exp1.raw().operator(), operator1);
        Assert.assertEquals(exp1.values().get(0).value(), value1);
    }

    @Test
    public void testGroupSingle() {
        String key = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator = com.cinchapi.concourse.thrift.Operator.EQUALS;
        Object value = Random.getObject();
        Criteria criteria = Criteria.where().key(key).operator(operator)
                .value(value).build();
        List<Symbol> symbols = Parsing
                .groupExpressions(Reflection.call(criteria, "symbols"));
        ExpressionSymbol exp = (ExpressionSymbol) symbols.get(0);
        Assert.assertEquals(1, symbols.size());
        Assert.assertEquals(exp.raw().key(), key);
        Assert.assertEquals(exp.raw().operator(), operator);
        Assert.assertEquals(exp.values().get(0).value(), value);
    }

    @Test
    public void testGroupSingleBetween() {
        String key = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator = com.cinchapi.concourse.thrift.Operator.BETWEEN;
        Object value = Random.getObject();
        Object value1 = Random.getObject();
        Criteria criteria = Criteria.where().key(key).operator(operator)
                .value(value).value(value1).build();
        List<Symbol> symbols = Parsing
                .groupExpressions(Reflection.call(criteria, "symbols"));
        ExpressionSymbol exp = (ExpressionSymbol) symbols.get(0);
        Assert.assertEquals(1, symbols.size());
        Assert.assertEquals(exp.raw().key(), key);
        Assert.assertEquals(exp.raw().operator(), operator);
        Assert.assertEquals(exp.values().get(0).value(), value);
        Assert.assertEquals(exp.values().get(1).value(), value1);
    }

    @Test
    public void testGroupSub() {
        String key0 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator0 = com.cinchapi.concourse.thrift.Operator.EQUALS;
        Object value0 = Random.getObject();
        String key1 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator1 = com.cinchapi.concourse.thrift.Operator.GREATER_THAN;
        Object value1 = Random.getObject();
        String key2 = Random.getString();
        com.cinchapi.concourse.thrift.Operator operator2 = com.cinchapi.concourse.thrift.Operator.LESS_THAN;
        Object value2 = Random.getObject();
        Criteria criteria = Criteria.where().key(key0).operator(operator0)
                .value(value0).and()
                .group(Criteria.where().key(key1).operator(operator1)
                        .value(value1).or().key(key2).operator(operator2)
                        .value(value2).build())
                .build();
        List<Symbol> symbols = Parsing
                .groupExpressions(Reflection.call(criteria, "symbols"));
        ExpressionSymbol exp0 = (ExpressionSymbol) symbols.get(0);
        ConjunctionSymbol sym1 = (ConjunctionSymbol) symbols.get(1);
        ParenthesisSymbol sym2 = (ParenthesisSymbol) symbols.get(2);
        ExpressionSymbol exp3 = (ExpressionSymbol) symbols.get(3);
        ConjunctionSymbol sym4 = (ConjunctionSymbol) symbols.get(4);
        ExpressionSymbol exp5 = (ExpressionSymbol) symbols.get(5);
        ParenthesisSymbol sym6 = (ParenthesisSymbol) symbols.get(6);
        Assert.assertEquals(7, symbols.size());
        Assert.assertEquals(exp0.raw().key(), key0);
        Assert.assertEquals(exp0.raw().operator(), operator0);
        Assert.assertEquals(exp0.values().get(0).value(), value0);
        Assert.assertEquals(ConjunctionSymbol.AND, sym1);
        Assert.assertEquals(ParenthesisSymbol.LEFT, sym2);
        Assert.assertEquals(exp3.raw().key(), key1);
        Assert.assertEquals(exp3.raw().operator(), operator1);
        Assert.assertEquals(exp3.values().get(0).value(), value1);
        Assert.assertEquals(ConjunctionSymbol.OR, sym4);
        Assert.assertEquals(exp5.raw().key(), key2);
        Assert.assertEquals(exp5.raw().operator(), operator2);
        Assert.assertEquals(exp5.values().get(0).value(), value2);
        Assert.assertEquals(ParenthesisSymbol.RIGHT, sym6);
    }

    @Test(expected = SyntaxException.class)
    public void testGroupSyntaxException() {
        List<Symbol> symbols = Lists.<Symbol> newArrayList(new KeySymbol("foo"),
                new KeySymbol("bar"));
        Parsing.groupExpressions(symbols);
    }

    @Test
    public void testParseCclAndOr() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value("1").and().key("b")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(2).or().key("c")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(3).build();
        String ccl = "a = '1' and b = 2 or c = 3";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclBetween() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(com.cinchapi.concourse.thrift.Operator.BETWEEN)
                .value("bar").value("baz").build();
        String ccl = "where foo bw bar baz";
        String ccl2 = "where foo >< bar baz";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
        Parser parser2 = createParser(ccl2);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser2.order());
    }

    @Test
    public void testParseCclBetweenWithBothReferences() {
        Criteria criteria = Criteria.where().key("age")
                .operator(com.cinchapi.concourse.thrift.Operator.BETWEEN)
                .value(30).value(35).build();
        String ccl = "where age bw $age $retireAge";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("age", 30);
        data.put("retireAge", 35);
        data.put("team", "Cleveland Cavaliers");
        Parser parser = createParser(ccl, data);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclBetweenWithFirstReference() {
        Criteria criteria = Criteria.where().key("age")
                .operator(com.cinchapi.concourse.thrift.Operator.BETWEEN)
                .value(30).value(100).build();
        String ccl = "where age bw $age 100";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");
        Parser parser = createParser(ccl, data);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclBetweenWithSecondReference() {
        Criteria criteria = Criteria.where().key("age")
                .operator(com.cinchapi.concourse.thrift.Operator.BETWEEN)
                .value(5).value(30).build();
        String ccl = "where age bw 5 $age";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");
        Parser parser = createParser(ccl, data);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCCLConjuctionsWithAnd() {
        String ccl = "name = chandresh pancholi on last christmas day && favovite_player != C. Ronaldo during last year";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        Assert.assertEquals(3, symbols.size());
        for (int i = 0; i < 2; i++) {
            ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
            Assert.assertTrue(
                    expr.values().get(0).value().toString().contains(" "));
            Assert.assertNotEquals(0, expr.raw().timestamp());
        }
    }

    @Test
    public void testParseCclGroupOrAndGroupOr() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .and()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        String ccl = "(a = 1 or b = 2) AND (c = 3 or d = 4)";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclGroupOrAndGroupOrConjuctions() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .and()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        String ccl = "(a = 1 || b = 2) && (c = 3 || d = 4)";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());

    }

    @Test
    public void testParseCclGroupOrAndGroupOrConjuctionsWithSingleAmpersand() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .and()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        String ccl = "(a = 1 || b = 2) & (c = 3 || d = 4)";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclGroupOrOrConjuction() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .or()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        String ccl = "(a = 1 || b = 2) || (c = 3 || d = 4)";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclGroupOrOrGroupOr() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .or()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        String ccl = "(a = 1 or b = 2) or (c = 3 or d = 4)";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test(expected = SyntaxException.class)
    public void testParseCclInvalidReference() {
        String ccl = "name = $name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("name", "King James");
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");
        Parser parser = createParser(ccl, data);
        Parsing.toPostfixNotation(parser.tokenize());
    }

    @Test
    public void testParseCclLocalReferences() {
        Criteria criteria = Criteria.where().key("name")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value("Lebron James").build();
        String ccl = "name = $name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("name", "Lebron James");
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");
        Parser parser = createParser(ccl, data);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test(expected = IllegalStateException.class)
    public void testParseCclNoSpaces() {
        String ccl = "name=jeff";
        Parser parser = createParser(ccl);
        parser.order();
    }

    @Test(expected = SyntaxException.class)
    public void testParseCclReferenceNotFound() {
        String ccl = "name = $name";
        Multimap<String, Object> data = LinkedHashMultimap.create();
        data.put("age", 30);
        data.put("team", "Cleveland Cavaliers");
        Parser parser = createParser(ccl, data);
        Parsing.toPostfixNotation(parser.tokenize());
    }

    @Test
    public void testParseCclSimple() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value("bar").build();
        String ccl = "where foo = bar";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());

    }

    @Test
    public void testParseCclSimpleAnd() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).and().key("b")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(2).build();
        String ccl = "a = 1 and b = 2";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclSimpleOr() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).or().key("b")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(2).build();
        String ccl = "a = 1 or b = 2";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testParseCclTimestampBasicPhrase() {
        String ccl = "name = jeff at \"now\"";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertNotEquals(0, expr.raw().timestamp()); // this means a
                                                           // timestamp was
                                                           // parsed
    }

    @Test
    public void testParseCclTimestampComplexPhrase() {
        String ccl = "name = jeff at \"last christmas\"";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertNotEquals(0, expr.raw().timestamp()); // this means a
                                                           // timestamp was
                                                           // parsed
    }

    @Test
    public void testParseCclTimestampNumericPhrase() {
        String ccl = "name = jeff at \"" + Time.now() + "\"";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertNotEquals(0, expr.raw().timestamp()); // this means a
                                                           // timestamp was
                                                           // parsed
    }

    @Test
    public void testParseCclTimestampPhraseWithoutQuotes() {
        String ccl = "name = jeff at 3 seconds ago";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertNotEquals(0, expr.raw().timestamp()); // this means a
                                                           // timestamp was
                                                           // parsed
    }

    @Test
    public void testParseCclValueAndTimestampPhraseWithoutQuotes() {
        String ccl = "name = jeff nelson on last christmas day";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertEquals("jeff nelson", expr.values().get(0).value());
        Assert.assertNotEquals(0, expr.raw().timestamp()); // this means a
                                                           // timestamp was
                                                           // parsed
    }

    @Test
    public void testParseCclValueAndTimestampPhraseWithoutQuotesAnd() {
        String ccl = "name = jeff nelson on last christmas day and favorite_player != Lebron James during last week";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        Assert.assertEquals(3, symbols.size());
        for (int i = 0; i < 2; ++i) {
            ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
            Assert.assertTrue(
                    expr.values().get(0).value().toString().contains(" "));
            Assert.assertNotEquals(0, expr.raw().timestamp()); // this means a
                                                               // timestamp was
                                                               // parsed
        }
    }

    @Test
    public void testParseCclValueWithoutQuotes() {
        String ccl = "name = jeff nelson";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertEquals("jeff nelson", expr.values().get(0).value());
    }

    @Test
    public void testParseCclValueWithoutQuotesAnd() {
        String ccl = "name = jeff nelson and favorite_player != Lebron James";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        Assert.assertEquals(3, symbols.size());
        for (int i = 0; i < 2; ++i) {
            ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
            Assert.assertTrue(
                    expr.values().get(0).value().toString().contains(" "));
        }
    }

    @Test
    public void testParserAnalysisIncludesAllCriteriaKeys() {
        String ccl = "name = jeff and age = 100 and company = cinchapi or company = blavity";
        Parser parser = createParser(ccl);
        Assert.assertEquals(Sets.newHashSet("name", "age", "company"),
                parser.analyze().keys());;
    }

    @Test
    public void testPostfixNotationAndGroupOr() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).and()
                .group(Criteria.where().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).or().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).build())
                .build();
        String ccl = "a = 1 and (b = 2 or c = 3)";
        Parser parser = createParser(ccl);
        Assert.assertEquals(
                Parsing.toPostfixNotation(Reflection.call(criteria, "symbols")),
                parser.order());
    }

    @Test
    public void testReproGH_113() {
        String ccl = "location = \"Atlanta (HQ)\"";
        Parser parser = createParser(ccl);
        Queue<PostfixNotationSymbol> symbols = parser.order();
        Assert.assertEquals(1, symbols.size());
        ExpressionSymbol expr = (ExpressionSymbol) symbols.poll();
        Assert.assertEquals("Atlanta (HQ)", expr.raw().values().get(0));
    }

    @Test
    public void testToPostfixNotationAndGroupOr() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).and()
                .group(Criteria.where().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).or().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).build())
                .build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 0)),
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(1)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 1)),
                ExpressionSymbol.create(new KeySymbol("b"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(2)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 2)),
                ExpressionSymbol.create(new KeySymbol("c"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(3)));
        Assert.assertEquals(Iterables.get(pfn, 3), ConjunctionSymbol.OR);
        Assert.assertEquals(Iterables.get(pfn, 4), ConjunctionSymbol.AND);

    }

    @Test
    public void testToPostfixNotationAndOr() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).and().key("b")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(2).or().key("c")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(3).build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(pfn.size(), 5);
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 0)),
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(1)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 1)),
                ExpressionSymbol.create(new KeySymbol("b"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(2)));
        Assert.assertEquals(Iterables.get(pfn, 2), ConjunctionSymbol.AND);
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 3)),
                ExpressionSymbol.create(new KeySymbol("c"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(3)));
        Assert.assertEquals(Iterables.get(pfn, 4), ConjunctionSymbol.OR);
    }

    @Test
    public void testToPostfixNotationGroupOrAndGroupOr() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .and()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 0)),
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(1)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 1)),
                ExpressionSymbol.create(new KeySymbol("b"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(2)));
        Assert.assertEquals(Iterables.get(pfn, 2), ConjunctionSymbol.OR);
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 3)),
                ExpressionSymbol.create(new KeySymbol("c"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(3)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 4)),
                ExpressionSymbol.create(new KeySymbol("d"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(4)));
        Assert.assertEquals(Iterables.get(pfn, 5), ConjunctionSymbol.OR);
        Assert.assertEquals(Iterables.get(pfn, 6), ConjunctionSymbol.AND);

    }

    @Test
    public void testToPostfixNotationGroupOrOrGroupOr() {
        Criteria criteria = Criteria.where()
                .group(Criteria.where().key("a")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(1).or().key("b")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(2).build())
                .or()
                .group(Criteria.where().key("c")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(3).or().key("d")
                        .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                        .value(4).build())
                .build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 0)),
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(1)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 1)),
                ExpressionSymbol.create(new KeySymbol("b"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(2)));
        Assert.assertEquals(Iterables.get(pfn, 2), ConjunctionSymbol.OR);
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 3)),
                ExpressionSymbol.create(new KeySymbol("c"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(3)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 4)),
                ExpressionSymbol.create(new KeySymbol("d"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(4)));
        Assert.assertEquals(Iterables.get(pfn, 5), ConjunctionSymbol.OR);
        Assert.assertEquals(Iterables.get(pfn, 6), ConjunctionSymbol.OR);

    }

    @Test
    public void testToPostfixNotationSimple() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value("bar").build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(pfn.size(), 1);
        Assert.assertEquals(
                ((ExpressionSymbol) Iterables.getOnlyElement(pfn)).key(),
                new KeySymbol("foo"));
        Assert.assertEquals(((ExpressionSymbol) Iterables.getOnlyElement(pfn))
                .values().get(0), new ValueSymbol("bar"));
        Assert.assertEquals(
                ((ExpressionSymbol) Iterables.getOnlyElement(pfn)).operator(),
                new OperatorSymbol(
                        com.cinchapi.concourse.thrift.Operator.EQUALS));
    }

    @Test
    public void testToPostfixNotationSimpleAnd() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).and().key("b")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(2).build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(pfn.size(), 3);
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 0)),
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(1)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 1)),
                ExpressionSymbol.create(new KeySymbol("b"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(2)));
        Assert.assertEquals(Iterables.get(pfn, 2), ConjunctionSymbol.AND);
    }

    @Test
    public void testToPostfixNotationSimpleBetween() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(com.cinchapi.concourse.thrift.Operator.BETWEEN)
                .value("bar").value("baz").build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(pfn.size(), 1);
        Assert.assertEquals(
                ((ExpressionSymbol) Iterables.getOnlyElement(pfn)).key(),
                new KeySymbol("foo"));
        Assert.assertEquals(((ExpressionSymbol) Iterables.getOnlyElement(pfn))
                .values().get(0), new ValueSymbol("bar"));
        Assert.assertEquals(((ExpressionSymbol) Iterables.getOnlyElement(pfn))
                .values().get(1), new ValueSymbol("baz"));
        Assert.assertEquals(
                ((ExpressionSymbol) Iterables.getOnlyElement(pfn)).operator(),
                new OperatorSymbol(
                        com.cinchapi.concourse.thrift.Operator.BETWEEN));
    }

    @Test
    public void testToPostfixNotationSimpleOr() {
        Criteria criteria = Criteria.where().key("a")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(1).or().key("b")
                .operator(com.cinchapi.concourse.thrift.Operator.EQUALS)
                .value(2).build();
        Queue<PostfixNotationSymbol> pfn = Parsing
                .toPostfixNotation(Reflection.call(criteria, "symbols"));
        Assert.assertEquals(pfn.size(), 3);
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 0)),
                ExpressionSymbol.create(new KeySymbol("a"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(1)));
        Assert.assertEquals(((ExpressionSymbol) Iterables.get(pfn, 1)),
                ExpressionSymbol.create(new KeySymbol("b"),
                        new OperatorSymbol(
                                com.cinchapi.concourse.thrift.Operator.EQUALS),
                        new ValueSymbol(2)));
        Assert.assertEquals(Iterables.get(pfn, 2), ConjunctionSymbol.OR);
    }

    @Test
    public void testAnalyzeKeysOperator() {
        String ccl = "name = jeff AND company = Cinchapi and age > 20 or name != bob";
        Parser parser = createParser(ccl);
        Assert.assertEquals(Sets.newHashSet("name", "company"),
                parser.analyze().keys(Operator.EQUALS));
    }

    @Test
    public void testAnalyzeOperators() {
        String ccl = "name = jeff AND company = Cinchapi and age > 20 or name != bob";
        Parser parser = createParser(ccl);
        Assert.assertEquals(Sets.newHashSet(Operator.EQUALS,
                Operator.GREATER_THAN, Operator.NOT_EQUALS),
                parser.analyze().operators());
    }

    @Test
    public void testConjunctionPrecedence() {
        String ccl = "name = jeff OR name = bob AND age > 100";
        Parser parser = createParser(ccl);
        AbstractSyntaxTree ast = parser.parse();
        Assert.assertEquals(ConjunctionSymbol.OR, ast.root());
    }

    @Test
    public void testParseLinksTo() {
        String ccl1 = "friend lnk2 1";
        String ccl2 = "friend lnks2 1";
        String ccl3 = "friend -> 1";
        Parser parser1 = createParser(ccl1);
        Parser parser2 = createParser(ccl2);
        Parser parser3 = createParser(ccl3);
        Assert.assertEquals(Sets.newHashSet(Operator.LINKS_TO),
                parser1.analyze().operators());
        Assert.assertEquals(Sets.newHashSet(Operator.LINKS_TO),
                parser2.analyze().operators());
        Assert.assertEquals(Sets.newHashSet(Operator.LINKS_TO),
                parser3.analyze().operators());
    }

    @Test
    public void testParseLikeOperator() {
        String ccl = "email like %gmail%";
        Parser parser = createParser(ccl);
        Assert.assertEquals(Sets.newHashSet(Operator.LIKE),
                parser.analyze().operators());
    }

    @Test
    public void testTokenizeUnquotedValueStringWithSpace() {
        Criteria criteria = Criteria.where().key("name")
                .operator(Operator.EQUALS).value("Jeff Nelson").and()
                .group(Criteria.where().key("company").operator(Operator.EQUALS)
                        .value("Cinchapi").or().key("company")
                        .operator(Operator.EQUALS).value("Blavity"))
                .build();
        String ccl = criteria.ccl();
        Parser parser = createParser(ccl);
        List<Symbol> symbols = parser.tokenize();
        Assert.assertEquals(Lists.newArrayList(new KeySymbol("name"),
                new OperatorSymbol(Operator.EQUALS),
                new ValueSymbol("Jeff Nelson"), ConjunctionSymbol.AND,
                ParenthesisSymbol.LEFT, new KeySymbol("company"),
                new OperatorSymbol(Operator.EQUALS),
                new ValueSymbol("Cinchapi"), ConjunctionSymbol.OR,
                new KeySymbol("company"), new OperatorSymbol(Operator.EQUALS),
                new ValueSymbol("Blavity"), ParenthesisSymbol.RIGHT), symbols);
    }

    @Test
    public void testParseSingleQuotedValue() {
        String ccl = "location = 'Atlanta (HQ)'";
        Parser parser = createParser(ccl);
        Assert.assertEquals("Atlanta (HQ)",
                ((ExpressionSymbol) parser.order().poll()).raw().values()
                        .get(0));
    }

    @Test
    public void testParseNumericString() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(Operator.EQUALS).value("17").build();
        Parser parser = createParser(criteria.ccl());
        List<Symbol> tokens = parser.tokenize();
        for (Symbol token : tokens) {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(String.class,
                        ((ValueSymbol) token).value().getClass());
            }
        }
    }

    @Test
    public void testParseNumericTag() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(Operator.EQUALS).value(Tag.create("17")).build();
        Parser parser = createParser(criteria.ccl());
        List<Symbol> tokens = parser.tokenize();
        for (Symbol token : tokens) {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(Tag.class,
                        ((ValueSymbol) token).value().getClass());
            }
        }
    }

    @Test
    public void testParseNonNumericTag() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(Operator.EQUALS).value(Tag.create("bar")).build();
        Parser parser = createParser(criteria.ccl());
        List<Symbol> tokens = parser.tokenize();
        for (Symbol token : tokens) {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(Tag.class,
                        ((ValueSymbol) token).value().getClass());
            }
        }
    }

    @Test
    public void testParseNumericNumber() {
        Criteria criteria = Criteria.where().key("foo")
                .operator(Operator.EQUALS).value(17).build();
        Parser parser = createParser(criteria.ccl());
        List<Symbol> tokens = parser.tokenize();
        for (Symbol token : tokens) {
            if(token instanceof ValueSymbol) {
                Assert.assertEquals(Integer.class,
                        ((ValueSymbol) token).value().getClass());
            }
        }
    }

    @Test
    public void testCriteriaWithTimestampValueParse() {
        Timestamp start = Timestamp.now();
        Timestamp end = Timestamp.now();
        Criteria criteria = Criteria.where().key("foo")
                .operator(Operator.BETWEEN).value(start).value(end).build();
        Parser parser = createParser(criteria.ccl());
        int count = 0;
        for (Symbol symbol : parser.tokenize()) {
            if(symbol instanceof ValueSymbol) {
                ValueSymbol $symbol = (ValueSymbol) symbol;
                Assert.assertEquals(Timestamp.class,
                        $symbol.value().getClass());
                Assert.assertTrue($symbol.value().equals(start)
                        || $symbol.value().equals(end));
                ++count;
            }
        }
        Assert.assertEquals(2, count);
    }

    protected abstract Parser createParser(String ccl);

    protected abstract Parser createParser(String ccl,
            Multimap<String, Object> data);

    private void doTestAbstractSyntaxTreeGeneration(String ccl) {
        Parser parser = createParser(ccl);
        Visitor<Queue<Symbol>> visitor = new Visitor<Queue<Symbol>>() {

            @SuppressWarnings("unchecked")
            @Override
            public Queue<Symbol> visit(ConjunctionTree tree, Object... data) {
                Queue<Symbol> queue = (Queue<Symbol>) data[0];
                tree.left().accept(this, data);
                tree.right().accept(this, data);
                queue.add(tree.root());
                return queue;
            }

            @SuppressWarnings("unchecked")
            @Override
            public Queue<Symbol> visit(ExpressionTree tree, Object... data) {
                Queue<Symbol> queue = (Queue<Symbol>) data[0];
                queue.add(tree.root());
                return queue;
            }

            @Override
            public Queue<Symbol> visit(CommandTree tree, Object... data) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Queue<Symbol> visit(OrderTree tree, Object... data) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Queue<Symbol> visit(PageTree tree, Object... data) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Queue<Symbol> visit(FunctionTree tree,
                    Object... data) {
                // TODO Auto-generated method stub
                return null;
            }
        };
        Queue<Symbol> queue = parser.parse().accept(visitor,
                new LinkedList<Symbol>());
        Assert.assertEquals(queue, parser.order());
    }

}