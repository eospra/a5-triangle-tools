package triangle.optimiser;

import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.expressions.BinaryExpression;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.IntegerExpression;
import triangle.abstractSyntaxTrees.expressions.UnaryExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.visitors.ActualParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.ActualParameterVisitor;
import triangle.abstractSyntaxTrees.visitors.ArrayAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.abstractSyntaxTrees.visitors.DeclarationVisitor;
import triangle.abstractSyntaxTrees.visitors.ExpressionVisitor;
import triangle.abstractSyntaxTrees.visitors.FormalParameterSequenceVisitor;
import triangle.abstractSyntaxTrees.visitors.IdentifierVisitor;
import triangle.abstractSyntaxTrees.visitors.LiteralVisitor;
import triangle.abstractSyntaxTrees.visitors.OperatorVisitor;
import triangle.abstractSyntaxTrees.visitors.ProgramVisitor;
import triangle.abstractSyntaxTrees.visitors.RecordAggregateVisitor;
import triangle.abstractSyntaxTrees.visitors.TypeDenoterVisitor;
import triangle.abstractSyntaxTrees.visitors.VnameVisitor;
import triangle.abstractSyntaxTrees.vnames.SimpleVname;

public class ExpressionHoister extends Hoister implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
		ActualParameterSequenceVisitor<Void, AbstractSyntaxTree>, ArrayAggregateVisitor<Void, AbstractSyntaxTree>,
		CommandVisitor<Void, AbstractSyntaxTree>, DeclarationVisitor<Void, AbstractSyntaxTree>,
		ExpressionVisitor<Void, AbstractSyntaxTree>, FormalParameterSequenceVisitor<Void, AbstractSyntaxTree>,
		IdentifierVisitor<Void, AbstractSyntaxTree>, LiteralVisitor<Void, AbstractSyntaxTree>,
		OperatorVisitor<Void, AbstractSyntaxTree>, ProgramVisitor<Void, AbstractSyntaxTree>,
		RecordAggregateVisitor<Void, AbstractSyntaxTree>, TypeDenoterVisitor<Void, AbstractSyntaxTree>,
		VnameVisitor<Void, AbstractSyntaxTree> {
	{

	}

	/**
	 * if both sides of the binary expression can be hoisted return the ast rather
	 * than a null
	 */
	@Override
	public AbstractSyntaxTree visitBinaryExpression(BinaryExpression ast, Void arg) {
		AbstractSyntaxTree replacement1 = ast.E1.visit(this);
		AbstractSyntaxTree replacement2 = ast.E2.visit(this);
		ast.O.visit(this);

		if (replacement1 != null && replacement2 != null) {
			System.out.println("not null be");
			return ast;
		} else if (replacement1 != null) {
			ast.E1 = (Expression) replacement1;
		} else if (replacement2 != null) {
			ast.E2 = (Expression) replacement2;
		}

		return null;
	}

	/**
	 * integer expressions allow for hoisting
	 */
	@Override
	public AbstractSyntaxTree visitIntegerExpression(IntegerExpression ast, Void arg) {
		return ast;
	}

	/**
	 * if the expression can be hoisted return the ast rather than null
	 */
	@Override
	public AbstractSyntaxTree visitUnaryExpression(UnaryExpression ast, Void arg) {
		AbstractSyntaxTree replacement = ast.E.visit(this);
		ast.O.visit(this);
		if (replacement != null) {
			return ast;
		}

		return null;
	}

	/**
	 * if the identifier is not updated return the ast rather than null
	 */
	@Override
	public AbstractSyntaxTree visitVnameExpression(VnameExpression ast, Void arg) {
		AbstractSyntaxTree replacement = ast.V.visit(this);
		if (replacement != null) {
			System.out.println("not null vne");
			return ast;
		} else {
			return null;
		}
	}

	/**
	 * if the identifier is not updated return the ast rather than null
	 */
	@Override
	public AbstractSyntaxTree visitSimpleVname(SimpleVname ast, Void arg) {
		AbstractSyntaxTree replacement = ast.I.visit(this);
		if (replacement != null) {
			System.out.println("not null svn");
			return ast;
		}
		return null;
	}

	/**
	 * check that the identifier is not on the list of updated identifiers
	 */
	@Override
	public AbstractSyntaxTree visitIdentifier(Identifier ast, Void arg) {
		if (updatedIdentifiers.contains(ast.spelling)) {
			return null;
		} else {
			System.out.println(ast.spelling);
			return ast;
		}
	}

	/**
	 * ignore the sequential command shenanagins in Hoister
	 */
	@Override
	public AbstractSyntaxTree visitSequentialCommand(SequentialCommand ast, Void arg) {
		ast.C1.visit(this);
		ast.C2.visit(this);
		return null;
	}

}
