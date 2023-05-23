# FuzzSceneSimTest
This repository manages the code of the scenario-based automatic driving model defect testing system. 

The system adopts B/S architecture, the front-end technology stack is Vue + Element UI + Echarts, and the back-end technology stack is SpringBoot + Maven + Mysql + Mybatis / MyBatis Plus.

## Repository Structure

+ This subdirectory `client` is responsible for storing and managing the front-end code

+ This subdirectory `server` is responsible for storing and managing the back-end code

+ This subdirectory `FuzzScene` is responsible for storing and managing the remote calling code on the Carla server running on the Ubuntu 20.04 system

+ `README.md`is a repository description file.


## Environment
+ Carla Server Env
  + System Env
    + We use **Ubuntu 20.04** as our server system
  + Python Env
    + We use **Anaconda** for switching to different experimental environments, because of the difference between **Carla** running environment (python 3.7) and the **Driving Model** environment (python 3.6)
    + We named the **Carla** anaconda environment `carla` and **Driving Model** environment `dave`
    + Two Anaconda environment files are saved in the `env` folder under the `FuzzScene `subdirectory
    
  + Carla & Scenario Runner Env
    + in order to conduct our experiment, we use **Carla Simulator-0.9.13** as our autonomous driving simulator, which can be found at https://github.com/carla-simulator/carla
    + in order to build simulation scenarios, we use **Scenario Runner-0.9.13** which can be found at https://github.com/carla-simulator/scenario_runner. The files **has already been** included in our code

+ Front-end && Back-end Server Env
  + Front-end Env
    + We use **Node.js v16.13.0** as front-end runtime environment
  + Back-end Env
    + We use **SpringBoot 2.6.2** and **jdk1.8** as back-end runtime environment and **maven-3.8.4** for dependencies management
    + We use **mysql-5.7.36** as back-end database

## Necessary Files

Because the trained model files in our code is too large, we provide Google Drive link to obtain these files in path *FuzzScene/code/sampling_vgg/* and *FuzzScene/code/trained_models/*

+ You can simply get these files through the link below
  + https://drive.google.com/drive/folders/18LgyPcKJOIQ-_ujr9vnNLoH0hW4I9RWo?usp=sharing
+ please make sure the files are in the right path consistent with the Google Drive

## Code Usage

+ Client
  
  To run front-end project on server, run commands under `client` root path :
  ```bash
  npm install
  npm run dev
  ```

+ Server

  First, create mysql tables using **springboot.sql** in `server` subdirectory

  Then, modify SSH configuration information in **server\src\main\java\com\nineya\springboot\constant\Constants.java** according to your carla server configuration; modify **application.yml** in root path according to your database configuration. 

  Last, to run back-end project on server, run command under `server` root path:
  ```bash
  mvn spring-boot:run
  ```

+ Carla

  Make sure the necessary files metioned above has been downloaded and put in the right paths.
  
  If there are still any problems, modify the path strings in the code according to the actual operating environment