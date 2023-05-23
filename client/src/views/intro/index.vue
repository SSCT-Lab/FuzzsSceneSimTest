<template xmlns="http://www.w3.org/1999/html">
  <div class="container">
    <el-row>
      <el-col :span="24">
        <p class="text">
          FuzzScene仿真测试系统是基于模糊测试理论设计并实现的一个用于自动驾驶系统的语义引导场景仿真测试工具。<br><br>
          以下是本工具相关的一些背景介绍：
        </p>
        <ul class="text">
          <li>自动驾驶系统：自动驾驶是通过环境感知、逻辑推理和决策、运动控制等实现车辆自我感知环境并安全移动的技术，以国内Apollo和国外Autoawre为代表的自动驾驶系统近年来发展迅速且已实现部分落地进行商业转化。
            一方面，机器视觉、模式识别和定位系统等技术进步，为自动驾驶技术的发展奠定了基石。另一方面，普及还存在一些如感知和决策算法的可靠性等关键技术问题需要解决 。</li><br>
          <li>OpenScenario：OpenScenario是一种自动驾驶场景生成语言，其使用XML文件格式定义了驾驶场景的静态对象和动态行为。
            其主要描述内容涉及车辆、行人和其他交通参与者等多个复杂的实体，也包括当前车辆和其余对象的动作（例如执行变道）或轨迹（例如，车道跟随）。也包含了对车辆属性的定义以及所处的复杂环境条件。
          </li><br>
          <li>Carla: Carla是一款开源的自动驾驶模拟器。从头开始构建了一系列模块化和灵活的API，以解决自动驾驶问题所涉及的一系列任务。
            CARLA的主要目标之一是作为一种用户可以轻松访问和定制的工具，帮助自动驾驶系统的研发和测试。
            CARLA基于UE4引擎来运行模拟，并使用OpenDRIVE标准来定义道路和城市环境，通过 Python 和 C++ 编写的 API实现了对模拟的控制。
          </li>
        </ul>
        <p class="text">
          FuzzScene仿真测试系统通过自动驾驶场景生成语言与模拟器进行联合仿真的方法，降低了复杂多样的现实驾驶场景的生成成本，并通过现实的变异算子来生成具有特定条件的测试用例，大大丰富了场景数据的多样性。<br><br>
          用户可以使用FuzzScene仿真测试系统，实现低成本、高效率地对自动驾驶系统进行测试，且通过使用检出的错误用例对模型进行重训练的方式可以显著提高自动驾驶系统的精度和可靠性。
        </p>
      </el-col>
    </el-row>
    <el-row type="flex" justify="left" :gutter="5">
      <el-col :span="9">
        <img src="../../../public/simScene.png" class="image">
      </el-col>
      <el-col :span="10">
        <img src="../../../public/apollo.png" class="image">
      </el-col>
      <el-col :span="8">
        <img src="../../../public/carla.gif" class="image">
      </el-col>
    </el-row>
    <br>
    <el-row type="flex" justify="left" >
      <el-col :span="9">
        <img src="../../../public/ori.png" class="image" style="width: 80%; height: 95%; margin-left: 50%">
      </el-col>
      <el-col :span="9">
        <img src="../../../public/mut.png" class="image" style="width: 80%; height: 95%; margin-left: 35%">
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: 'Index'
}
</script>

<style scoped lang='scss'>
.container {
  margin: 20px;
}
.text {
  //font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 17px;
  font-weight: normal;
  color: #303133;
  line-height: 140%;
  //text-indent: 2em;
  //text-align: center;
}
.image {
  width: 100%;
  height: 100%;
  float: left;
  margin-left: 0%;
}
</style>
