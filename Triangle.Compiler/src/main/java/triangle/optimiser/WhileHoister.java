package triangle.optimiser;

import triangle.StdEnvironment;
import triangle.abstractSyntaxTrees.AbstractSyntaxTree;
import triangle.abstractSyntaxTrees.commands.AssignCommand;
import triangle.abstractSyntaxTrees.commands.SequentialCommand;
import triangle.abstractSyntaxTrees.declarations.ConstDeclaration;
import triangle.abstractSyntaxTrees.declarations.Declaration;
import triangle.abstractSyntaxTrees.declarations.VarDeclaration;
import triangle.abstractSyntaxTrees.expressions.Expression;
import triangle.abstractSyntaxTrees.expressions.LetExpression;
import triangle.abstractSyntaxTrees.expressions.VnameExpression;
import triangle.abstractSyntaxTrees.terminals.Identifier;
import triangle.abstractSyntaxTrees.types.AnyTypeDenoter;
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

public class WhileHoister extends Hoister implements ActualParameterVisitor<Void, AbstractSyntaxTree>,
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
	 * check the assignment expression for updated identifiers; ideally this method
	 * would also set the expression equal to the constant that would be created.
	 * The code in the if statement results in a let expression, not a command, and
	 * is obviously not in the right place, but if shows how the let command might
	 * be implemented elsewhere in the program with different class types
	 * 
	 */
	@Override
	public AbstractSyntaxTree visitAssignCommand(AssignCommand ast, Void arg) {
		ast.V.visit(this);
		AbstractSyntaxTree replacement = ast.E.visit(new ExpressionHoister());
		if (replacement != null) {
			System.out.println("not null ac");
			Identifier tmp = new Identifier("tmp", ast.getPosition());
			VarDeclaration tmpDecl = new VarDeclaration(tmp, new AnyTypeDenoter(ast.getPosition()), ast.getPosition());
			LetExpression hoist = new LetExpression(tmpDecl, (Expression) replacement, ast.getPosition());
			hoist.type = StdEnvironment.anyType;
			ast.E = hoist;
			return ast;
		}
		return null;
	}

	/**
	 * add the assigned/updated identifiers to the list
	 */
	@Override
	public AbstractSyntaxTree visitIdentifier(Identifier ast, Void arg) {
		updatedIdentifiers.add(ast.spelling);
		System.out.println(updatedIdentifiers.toString());
		return null;
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
