package com.founder.fix.fixflow.expand.connector.TimeoutHandling;


import java.util.List;

import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.UserTask;

import com.founder.fix.fixflow.core.runtime.ExecutionContext;
import com.founder.fix.fixflow.core.action.ConnectorHandler;
import com.founder.fix.fixflow.core.exception.FixFlowException;
import com.founder.fix.fixflow.core.impl.bpmn.behavior.TaskCommandInst;
import com.founder.fix.fixflow.core.impl.bpmn.behavior.UserTaskBehavior;
import com.founder.fix.fixflow.core.impl.expression.ExpressionMgmt;
import com.founder.fix.fixflow.core.impl.identity.Authentication;
import com.founder.fix.fixflow.core.impl.runtime.TokenEntity;
import com.founder.fix.fixflow.core.impl.task.TaskInstanceEntity;
import com.founder.fix.fixflow.core.impl.util.StringUtil;

public class TimeoutHandling implements ConnectorHandler {

	private java.lang.String commandId;

	private java.lang.String opinion;

	public void execute(ExecutionContext executionContext) throws Exception {
		
		FlowNode flowNode=executionContext.getTimeOutNode();
		if(flowNode==null){
			flowNode=executionContext.getToken().getFlowNode();
		}else{
			executeOld(executionContext);
			return;
		}
		UserTaskBehavior userTask=null;
		if(flowNode instanceof UserTask){
			userTask=(UserTaskBehavior)flowNode;
		}
		if(userTask==null){
			return;
		}
		
		if(commandId==null){
			return;
		}
		else{
			TaskCommandInst taskCommandInst=userTask.getTaskCommandsMap().get(commandId);
			
			
			if (taskCommandInst != null && taskCommandInst.getExpression() != null) {
				try {
					
					ExpressionMgmt.execute(taskCommandInst.getExpression(), executionContext);
				} catch (Exception e) {
					throw new FixFlowException("用户命令表达式执行异常!", e);
				}
			}
			
			
			TaskInstanceEntity taskInstanceImpl=(TaskInstanceEntity)executionContext.getTaskInstance();
			
			
			if(taskInstanceImpl!=null){
				
				taskInstanceImpl.setAssigneeWithoutCascade(Authentication.getAuthenticatedUserId());
				
				taskInstanceImpl.end();
				taskInstanceImpl.setDraft(false);
				taskInstanceImpl.setCommandId(commandId);
				taskInstanceImpl.setCommandType(StringUtil.getString(taskCommandInst.getTaskCommandType()));
				taskInstanceImpl.setCommandMessage(taskCommandInst.getName());
				taskInstanceImpl.setTaskComment(this.opinion);
				taskInstanceImpl.setAgent(null);

				
			}
		}
		
		
		
	}
	
	public void executeOld(ExecutionContext executionContext) throws Exception {
		
		FlowNode flowNode=executionContext.getTimeOutNode();

		UserTaskBehavior userTask=null;
		TokenEntity token=executionContext.getToken();
		if(flowNode instanceof UserTask){
			userTask=(UserTaskBehavior)flowNode;
		}
		if(userTask==null){
			return;
		}
		
		if(commandId==null){
			return;
		}
		else{
			TaskCommandInst taskCommandInst=userTask.getTaskCommandsMap().get(commandId);
			
			
			if (taskCommandInst != null && taskCommandInst.getExpression() != null) {
				try {
					
					ExpressionMgmt.execute(taskCommandInst.getExpression(), executionContext);
				} catch (Exception e) {
					throw new FixFlowException("用户命令表达式执行异常!", e);
				}
			}
			
			
			List<TaskInstanceEntity> taskInstances= token.getProcessInstance().getTaskMgmtInstance().getTaskInstanceEntitys(token);
			for (TaskInstanceEntity taskInstanceEntity : taskInstances) {
				if(taskInstanceEntity.hasEnded()){
					taskInstanceEntity.setCommandId(taskCommandInst.getId());
					taskInstanceEntity.setCommandMessage(opinion);
					taskInstanceEntity.setCommandType(taskCommandInst.getTaskCommandType());
				}
			}
		}
		
		
		
	}
	
	

	public void  setCommandId(java.lang.String commandId){
		this.commandId = commandId;
	}

	public void  setOpinion(java.lang.String opinion){
		this.opinion = opinion;
	}

}