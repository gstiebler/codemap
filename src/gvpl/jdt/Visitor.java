package gvpl.jdt;

import java.util.Iterator;

import org.eclipse.jdt.core.dom.*;

public class Visitor extends ASTVisitor {

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter
					.next();
			IVariableBinding binding = fragment.resolveBinding();
			if(binding != null)
				System.out.println("sucesso " + binding.getName());
			// first assignment is the initalizer
		}
	}
	
	public boolean visit(Assignment node) {
		if (node.getLeftHandSide() instanceof SimpleName) {
			IBinding binding = ((SimpleName) node.getLeftHandSide())
					.resolveBinding();

			if(binding != null)
				System.out.println("Assign left " + binding.getName());
		}
		if (node.getRightHandSide() instanceof SimpleName) {
			IBinding binding = ((SimpleName) node.getRightHandSide())
					.resolveBinding();

			if(binding != null)
				System.out.println("Assign right " + binding.getName());
		}
		// prevent that simplename is interpreted as reference
		return true;
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		SimpleName name = node.getName();
		
		if(node.resolveBinding() != null)
			System.out.println("funcionou");
		
		IBinding binding = name.resolveBinding();
		if(binding != null)
		{
			String temp = binding.getName();
			System.out.println("VarDeclFrag " + temp);
		}
		return true; // do not continue 
	}
	
	@Override
	public void endVisit(SimpleName node) {
		
		IBinding binding = node.resolveBinding();
		if(binding != null)
			System.out.println("Simple name: " + binding.getName());
	}
	
	@Override
	public void endVisit(NumberLiteral node) {
		System.out.println("Token: " + node.getToken());
	}
	
	@Override
	public void endVisit(InfixExpression node) {
		System.out.println("Infix: " + node.toString());
	}
	
	public boolean visit(ForStatement node) {
		System.out.println("Entrou loop");
		return true;
	}
	
	public void endVisit(ForStatement node) {
		System.out.println("saiu loop");
	}
	
}
