package com.drinkwater.reminder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drinkwater.reminder.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("使用帮助") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Blue700, titleContentColor = White
                )
            )
        },
        containerColor = Gray50
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HelpSection("喝水提醒") {
                Text("· 首页查看每日喝水进度，点击「记录喝水」计数")
                Text("· 在设置中调整提醒间隔（15-120分钟）和每日目标（4-20杯）")
                Text("· 开启浮窗后，屏幕边缘出现水滴图标，点击即可记录")
                Text("· 设置「免打扰时段」后，该时段内不发送喝水提醒")
            }

            HelpSection("打卡提醒") {
                Text("· 在「提醒」页开启打卡提醒模块")
                Text("· 开启后通知栏显示「打卡提醒运行中」，保持后台运行")
                Text("· 每次解锁屏幕自动弹出班次选择（早班/晚班/通班/休息）")
                Text("· 选择后自动设置上下班两个提醒时间")
                Text("· 每个班次可分别配置上班和下班时间")
                Text("· 选择「休息」则该天不提醒")
            }

            HelpSection("久坐提醒") {
                Text("· 在「提醒」页开启久坐提醒模块")
                Text("· 可设置提醒间隔（15-90分钟）和生效时段")
                Text("· 仅在设定时段内发送提醒，其他时间静默")
            }

            HelpSection("睡前提醒 & 锁屏") {
                Text("· 在「提醒」页开启睡前提醒模块")
                Text("· 设置睡觉时间和起床时间")
                Text("· 开启「软锁屏」后，到点弹出全屏遮罩")
                Text("· 紧急解除方式：长按「紧急使用」3秒 或 连续按返回键3次")
                Text("· 解除后有15分钟宽限期，起床时间到自动恢复")
            }

            HelpSection("自定义提醒") {
                Text("· 在「提醒」页点击右下角 + 号创建")
                Text("· 支持一次性、每天、每周三种重复模式")
                Text("· 每周模式可选择星期一至星期日的任意组合")
                Text("· 可设置标签名称方便识别")
            }

            HelpSection("手环联动（可选）") {
                Text("· 在设置中开启「手环震动」")
                Text("· 打开手环配套的运动健康 App")
                Text("· 进入设备 → 消息通知 → 开启「日常提醒助手」的通知权限")
                Text("· 手机收到提醒时，手环会自动同步震动")
            }

            HelpSection("手机厂商适配（重要）") {
                Text("· 华为/荣耀：设置 → 应用 → 应用启动管理 → 手动管理（允许自启动、关联启动、后台活动）")
                Text("· 小米/红米：安全中心 → 应用管理 → 权限 → 自启动管理 → 开启")
                Text("· OPPO/一加：设置 → 电池 → 应用耗电管理 → 允许后台运行")
                Text("· vivo/iQOO：设置 → 电池 → 后台高耗电 → 允许")
                Text("· 以上设置可确保提醒准时触发、后台服务不被杀掉")
            }

            HelpSection("权限说明") {
                Text("· 通知权限：发送提醒通知（必需）")
                Text("· 浮窗权限：显示水滴浮窗和睡前锁屏遮罩")
                Text("· 开机自启：重启手机后自动恢复提醒")
                Text("· 蓝牙权限：预留手环联动（不会主动连接蓝牙设备）")
                Text("· 本 App 不会收集任何个人信息，所有数据仅存储在手机本地")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Blue700
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}
