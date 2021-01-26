package com.chris.web;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipInputStream;

/**
 * Author: shaoqing
 * date-time: 2021-01-26 22:32
 **/
public class TestActiviti {
    private  ProcessEngine processEngine;

    /**
     * 使用代码创建工作流使用的23张表
     */
    @Test
    public void createTable(){
        //创建引擎配置类
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        configuration.setJdbcDriver("com.mysql.jdbc.Driver");
        configuration.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/test");
        configuration.setJdbcUsername("root");
        configuration.setJdbcPassword("root");

        //不自动创建表，需要表存在 DB_SCHEMA_UPDATE_FALSE = "false";
        //先删除表，再创建表 DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";
        //如果表不存在，先创建表 DB_SCHEMA_UPDATE_TRUE = "true";
        configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        //创建工作流核心对象
        ProcessEngine processEngine = configuration.buildProcessEngine();
        System.out.println(processEngine);
    }

    @Test
    public void getService(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        //管理流程定义
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //执行管理，包括启动、推进、删除流程实例等
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //任务管理
        TaskService taskService = processEngine.getTaskService();
        //历史管理（执行完的数据的管理
        HistoryService historyService = processEngine.getHistoryService();
        //组织机构管理
        IdentityService identityService = processEngine.getIdentityService();
        //可选服务，任务表单管理
        FormService formService = processEngine.getFormService();

        ManagementService managementService = processEngine.getManagementService();
    }

    @Test
    public void testRespositoryService(){
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //可产生DeploymentBuilder用来定义流程部署的相关参数
        DeploymentBuilder deployment = repositoryService.createDeployment();
        //删除流程定义
        repositoryService.deleteDeployment("deploymentId");
    }

    /**
     * 部署流程定义
     */
    @Test
    public void deploymentProcessDefinition(){
        processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取流程定义与部署相关Service
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()     //创建一个部署对象
                .name("helloworld入门程序")
                .addClasspathResource("diagrams/helloworld.bpmn")//加载资源文件
                .deploy();//完成部署
        System.out.println(deployment.getId());
        System.out.println(deployment.getName());
    }

    /**
     * 启动流程实例
     */
    @Test
    public void startProcessInstance(){
        //获取与正在执行的流程示例和执行对象相关的Service
        ProcessInstance processInstance = processEngine.getRuntimeService()
                //使用流程定义的key启动实例，key对应bpmn文件中id的属性值，默认按照最新版本流程启动
                .startProcessInstanceByKey("helloworld");
        System.out.println(processInstance.getId());
        System.out.println(processInstance.getProcessDefinitionId());
    }

    /**
     * 查询当前的个人任务
     */
    @Test
    public void findPersonalTask(){
        //与正在执行的任务相关的Service
        List<Task> list = processEngine.getTaskService()
                .createTaskQuery()  //创建查询任务对象
                .taskAssignee("王五")     //指定个人任务查询，指定办理人
                .list();
        if(list != null && list.size() > 0){
            for(Task task : list){
                System.out.println(task.getId());
                System.out.println(task.getName());
                System.out.println(task.getCreateTime());
                System.out.println(task.getAssignee());
                System.out.println(task.getProcessInstanceId());
                System.out.println(task.getExecutionId());
                System.out.println(task.getProcessDefinitionId());
            }
        }
    }
    /**
     * 完成我的任务
     */
    @Test
    public void completePersonalTask(){
        processEngine.getTaskService()
                .complete("7502");
    }

    /**部署流程定义（从classpath）*/
    @Test
    public void deploymentProcessDefinition_classpath(){
        Deployment deployment = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("流程定义")//添加部署的名称
                .addClasspathResource("diagrams/helloworld.bpmn")//从classpath的资源中加载，一次只能加载一个文件
                .addClasspathResource("diagrams/helloworld.png")//从classpath的资源中加载，一次只能加载一个文件
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());//
        System.out.println("部署名称："+deployment.getName());//
    }

    /**部署流程定义（从zip）*/
    @Test
    public void deploymentProcessDefinition_zip(){
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("diagrams/helloworld.zip");
        ZipInputStream zipInputStream = new ZipInputStream(in);
        Deployment deployment = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createDeployment()//创建一个部署对象
                .name("流程定义")//添加部署的名称
                .addZipInputStream(zipInputStream)//指定zip格式的文件完成部署
                .deploy();//完成部署
        System.out.println("部署ID："+deployment.getId());//
        System.out.println("部署名称："+deployment.getName());//
    }

    /**查询流程定义*/
    @Test
    public void findProcessDefinition(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()//与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()//创建一个流程定义的查询
                /**指定查询条件,where条件*/
//                        .deploymentId(deploymentId)//使用部署对象ID查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
//                        .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /**排序*/
                .orderByProcessDefinitionVersion().asc()//按照版本的升序排列
//                        .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                /**返回的结果集*/
                .list();//返回一个集合列表，封装流程定义
//                        .singleResult();//返回惟一结果集
//                        .count();//返回结果集数量
//                        .listPage(firstResult, maxResults);//分页查询
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:"+pd.getResourceName());
                System.out.println("资源名称png文件:"+pd.getDiagramResourceName());
                System.out.println("部署对象ID："+pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
    }

    /**删除流程定义*/
    @Test
    public void deleteProcessDefinition(){
        //使用部署ID，完成删除
        String deploymentId = "601";
        /**
         * 不带级联的删除
         *    只能删除没有启动的流程，如果流程启动，就会抛出异常
         */
//        processEngine.getRepositoryService()//
//                        .deleteDeployment(deploymentId);

        /**
         * 级联删除
         *       不管流程是否启动，都能可以删除
         */
        processEngine.getRepositoryService()//
                .deleteDeployment(deploymentId, true);
        System.out.println("删除成功！");
    }

    /**查看流程图
     * @throws IOException */
    @Test
    public void viewPic() throws IOException {
        /**将生成图片放到文件夹下*/
        String deploymentId = "801";
        //获取图片资源名称
        List<String> list = processEngine.getRepositoryService()//
                .getDeploymentResourceNames(deploymentId);
        //定义图片资源的名称
        String resourceName = "";
        if(list!=null && list.size()>0){
            for(String name:list){
                if(name.indexOf(".png")>=0){
                    resourceName = name;
                }
            }
        }
        //获取图片的输入流
        InputStream in = processEngine.getRepositoryService()//
                .getResourceAsStream(deploymentId, resourceName);
        //将图片生成到D盘的目录下
        File file = new File("D:/"+resourceName);
        //将输入流的图片写到D盘下
        FileUtils.copyInputStreamToFile(in, file);
    }
    /***附加功能：查询最新版本的流程定义*/
    @Test
    public void findLastVersionProcessDefinition(){
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .orderByProcessDefinitionVersion().asc()//使用流程定义的版本升序排列
                .list();
        /**
         * Map<String,ProcessDefinition>
         map集合的key：流程定义的key
         map集合的value：流程定义的对象
         map集合的特点：当map集合key值相同的情况下，后一次的值将替换前一次的值
         */
        Map<String, ProcessDefinition> map = new LinkedHashMap<String, ProcessDefinition>();
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                map.put(pd.getKey(), pd);
            }
        }
        List<ProcessDefinition> pdList = new ArrayList<ProcessDefinition>(map.values());
        if(pdList!=null && pdList.size()>0){
            for(ProcessDefinition pd:pdList){
                System.out.println("流程定义ID:"+pd.getId());//流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:"+pd.getName());//对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:"+pd.getKey());//对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:"+pd.getVersion());//当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:"+pd.getResourceName());
                System.out.println("资源名称png文件:"+pd.getDiagramResourceName());
                System.out.println("部署对象ID："+pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }
    }

    /**附加功能：删除流程定义（删除key相同的所有不同版本的流程定义）*/
    @Test
    public void deleteProcessDefinitionByKey(){
        //流程定义的key
        String processDefinitionKey = "helloworld";
        //先使用流程定义的key查询流程定义，查询出所有的版本
        List<ProcessDefinition> list = processEngine.getRepositoryService()//
                .createProcessDefinitionQuery()//
                .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
                .list();
        //遍历，获取每个流程定义的部署ID
        if(list!=null && list.size()>0){
            for(ProcessDefinition pd:list){
                //获取部署ID
                String deploymentId = pd.getDeploymentId();
                processEngine.getRepositoryService()//
                        .deleteDeployment(deploymentId, true);
            }
        }
    }

    /**
     * 启动流程实例
     */
    @Test
    public void startProcessInstance1(){
        //获取与正在执行的流程示例和执行对象相关的Service
        ProcessInstance processInstance = processEngine.getRuntimeService()
                //使用流程定义的key启动实例，key对应bpmn文件中id的属性值，默认按照最新版本流程启动
                .startProcessInstanceByKey("helloworld");
        System.out.println(processInstance.getId());
        System.out.println(processInstance.getProcessDefinitionId());
    }
    /**查询当前人的个人任务*/
    @Test
    public void findMyPersonalTask(){
        String assignee = "张三";
        List<Task> list = processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .createTaskQuery()//创建任务查询对象
                /**查询条件（where部分）*/
                .taskAssignee(assignee)//指定个人任务查询，指定办理人
//                        .taskCandidateUser(candidateUser)//组任务的办理人查询
//                        .processDefinitionId(processDefinitionId)//使用流程定义ID查询
//                        .processInstanceId(processInstanceId)//使用流程实例ID查询
//                        .executionId(executionId)//使用执行对象ID查询
                /**排序*/
                .orderByTaskCreateTime().asc()//使用创建时间的升序排列
                /**返回结果集*/
//                        .singleResult()//返回惟一结果集
//                        .count()//返回结果集的数量
//                        .listPage(firstResult, maxResults);//分页查询
                .list();//返回列表
        if(list!=null && list.size()>0){
            for(Task task:list){
                System.out.println("任务ID:"+task.getId());
                System.out.println("任务名称:"+task.getName());
                System.out.println("任务的创建时间:"+task.getCreateTime());
                System.out.println("任务的办理人:"+task.getAssignee());
                System.out.println("流程实例ID："+task.getProcessInstanceId());
                System.out.println("执行对象ID:"+task.getExecutionId());
                System.out.println("流程定义ID:"+task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        }
    }

    /**完成我的任务*/
    @Test
    public void completeMyPersonalTask(){
        //任务ID
        String taskId = "1202";
        processEngine.getTaskService()//与正在执行的任务管理相关的Service
                .complete(taskId);
        System.out.println("完成任务：任务ID："+taskId);
    }

    /**查询流程状态（判断流程正在执行，还是结束）*/
    @Test
    public void isProcessEnd(){
        String processInstanceId = "1001";
        ProcessInstance pi = processEngine.getRuntimeService()//表示正在执行的流程实例和执行对象
                .createProcessInstanceQuery()//创建流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        if(pi==null){
            System.out.println("流程已经结束");
        }
        else{
            System.out.println("流程没有结束");
        }
    }

    /**查询历史任务*/
    @Test
    public void findHistoryTask(){
        String taskAssignee = "张三";
        List<HistoricTaskInstance> list = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                .createHistoricTaskInstanceQuery()//创建历史任务实例查询
                .taskAssignee(taskAssignee)//指定历史任务的办理人
                .list();
        if(list!=null && list.size()>0){
            for(HistoricTaskInstance hti:list){
                System.out.println(hti.getId()+"    "+hti.getName()+"    "+hti.getProcessInstanceId()+"   "+hti.getStartTime()+"   "+hti.getEndTime()+"   "+hti.getDurationInMillis());
                System.out.println("################################");
            }
        }
    }

    /**查询历史流程实例*/
    @Test
    public void findHistoryProcessInstance(){
        String processInstanceId = "1001";
        HistoricProcessInstance hpi = processEngine.getHistoryService()//与历史数据（历史表）相关的Service
                .createHistoricProcessInstanceQuery()//创建历史流程实例查询
                .processInstanceId(processInstanceId)//使用流程实例ID查询
                .singleResult();
        System.out.println(hpi.getId()+"    "+hpi.getProcessDefinitionId()+"    "+hpi.getStartTime()+"    "+hpi.getEndTime()+"     "+hpi.getDurationInMillis());
    }

    /**模拟设置和获取流程变量的场景 */
    @Test
    public void setAndGetVariables(){
        RuntimeService runtimeService = processEngine.getRuntimeService();
        TaskService taskService = processEngine.getTaskService();

//        //使用执行对象ID设置
//        runtimeService.setVariable(executionId, variableName, value);（设置一个）
//        runtimeService.setVariables(executionId, variables);
//
//        //使用任务ID设置
//        taskService.setVariable(taskId, variableName, value);（设置一个）
//        taskService.setVariables(taskId, variables);
//
//        //启动流程实例的同时设置
//        runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
//
//        //完成任务的同时设置
//        taskService.complete(taskId, variables);
//
//        /**获取流程变量*/
//        //使用执行对象ID和流程变量的名称，获取流程变量的值
//        runtimeService.getVariable(executionId, variableName);
//        //使用执行对象ID，获取所有的流程变量，将流程变量放置到Map集合中，map集合的key就是流程变量的名称，map集合的value就是流程变量的值
//        runtimeService.getVariables(executionId);
//        //使用执行对象ID，获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到Map集合中
//        runtimeService.getVariables(executionId, variableNames);
//
//        //使用任务ID和流程变量的名称，获取流程变量的值
//        taskService.getVariable(taskId, variableName);
//        //使用任务ID，获取所有的流程变量，将流程变量放置到Map集合中，map集合的key就是流程变量的名称，map集合的value就是流程变量的值
//        taskService.getVariables(taskId);
//        //使用任务ID，获取流程变量的值，通过设置流程变量的名称存放到集合中，获取指定流程变量名称的流程变量的值，值存放到Map集合中
//        taskService.getVariables(taskId, variableNames);
    }
    /**设置流程变量 */
    @Test
    public void setVariables(){
        TaskService taskService = processEngine.getTaskService();
        //任务ID
        String taskId = "50004";
        //一、设置流程变量，使用基本数据类型
        taskService.setVariableLocal(taskId,"请假天数",3);//local与当前task绑定，下一个task不可见
        taskService.setVariable(taskId,"请假日期",new Date());
        taskService.setVariable(taskId,"请假原因","回家探亲");
        //二：设置流程变量，使用javabean类型
        /**
         * 当一个javabean（实现序列号）放置到流程变量中，要求javabean的属性不能再发生变化
         *    * 如果发生变化，再获取的时候，抛出异常
         *
         * 解决方案：在Person对象中添加：
         *         private static final long serialVersionUID = 6757393795687480331L;
         *      同时实现Serializable
//         * */
//        Person p = new Person();
//        p.setId(20);
//        p.setName("翠花");
//        taskService.setVariable(taskId, "人员信息(添加固定版本)", p);

        System.out.println("流程变量设置成功");
    }
    /**查询流程变量的历史表*/
    @Test
    public void findHistoryProcessVariables(){
        List<HistoricVariableInstance> list = processEngine.getHistoryService()//
                .createHistoricVariableInstanceQuery()//创建一个历史的流程变量查询对象
                .variableName("请假天数")
                .list();
        if(list!=null && list.size()>0){
            for(HistoricVariableInstance hvi:list){
                System.out.println(hvi.getId()+"   "+hvi.getProcessInstanceId()+"   "+hvi.getVariableName()+"   "+hvi.getVariableTypeName()+"    "+hvi.getValue());
                System.out.println("###############################################");
            }
        }
    }
}