import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

/* Layout */
import Layout from '@/layout'

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
    roles: ['admin','editor']    control the page roles (you can set multiple roles)
    title: 'title'               the name show in sidebar and breadcrumb (recommend set)
    icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
    breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
    activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
  }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },

  {
    path: '/',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: 'index',
        name: 'intro',
        component: () => import('@/views/intro/index'),
        meta: { title: '系统介绍', icon: 'international' }
      }
    ]
  },

  {
    path: '/model',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Dashboard2',
        component: () => import('@/views/model/modelList'),
        meta: { title: '测试模型管理', icon: 'file', roles: ['editor'] }
      }
    ]
  },

  {
    path: '/seed',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Dashboard2',
        component: () => import('@/views/seed/seedList.vue'),
        meta: { title: '种子场景管理', icon: 'file2' }
      }
    ]
  },

  {
    path: '/sceneSim',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Form',
        component: () => import('@/views/simulation/sceneSimulate.vue'),
        meta: { title: '场景可视化仿真', icon: 'form' }
      }
    ]
  },

  {
    path: '/testTask',
    component: Layout,
    children: [
      {
        path: 'create',
        component: () => import('@/views/config/testConfiguration.vue'),
        name: 'Menu2',
        meta: { title: '创建测试任务', icon: 'nested' }
      }
    ]
  },

  {
    path: '/task',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Form',
        component: () => import('@/views/task/taskManage'),
        meta: { title: '测试任务管理', icon: 'form' }
      }
    ]
  },

  {
    path: '/result',
    component: Layout,
    children: [
      {
        path: 'index',
        name: 'Form',
        component: () => import('@/views/dashboard/index'),
        meta: { title: '测试结果展示', icon: 'el-icon-s-help' },
        hidden: true
      }
    ]
  },
  //
  // {
  //   path: '/example',
  //   component: Layout,
  //   redirect: '/example/table',
  //   name: 'Example',
  //   meta: { title: '结果展示', icon: 'el-icon-s-help' },
  //   children: [
  //     {
  //       path: 'table',
  //       name: 'Table',
  //       component: () => import('@/views/table/index'),
  //       meta: { title: '雷达图', icon: 'radar' }
  //     },
  //     {
  //       path: 'tree',
  //       name: 'Tree',
  //       component: () => import('@/views/tree/index'),
  //       meta: { title: '折线图', icon: 'line' }
  //     },
  //     {
  //       path: 'bar',
  //       name: 'Bar',
  //       component: () => import('@/views/tree/index'),
  //       meta: { title: '柱状图', icon: 'bar' }
  //     }
  //   ]
  // },

  // {
  //   path: 'external-link',
  //   component: Layout,
  //   children: [
  //     {
  //       path: 'https://panjiachen.github.io/vue-element-admin-site/#/',
  //       meta: { title: 'External Link', icon: 'link' }
  //     }
  //   ]
  // },

  // 404 page must be placed at the end !!!
  { path: '*', redirect: '/404', hidden: true }
]

const createRouter = () => new Router({
  // mode: 'history', // require service support
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRoutes
})

const router = createRouter()

// Detail see: https://github.com/vuejs/vue-router/issues/1234#issuecomment-357941465
export function resetRouter() {
  const newRouter = createRouter()
  router.matcher = newRouter.matcher // reset router
}

export default router
