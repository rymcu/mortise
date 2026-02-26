<script setup lang="ts">
/**
 * 通知设置页面
 */
const state = reactive({
	systemNotice: true,
	securityNotice: true,
	emailNotice: false
})
</script>

<template>
	<UDashboardPanel id="settings-notifications">
		<template #header>
			<UDashboardNavbar title="消息设置">
				<template #leading>
					<UDashboardSidebarCollapse />
				</template>
			</UDashboardNavbar>
		</template>

		<template #body>
			<div class="mx-auto max-w-2xl space-y-6">
				<UCard>
					<template #header>
						<h3 class="text-lg font-semibold">通知偏好</h3>
					</template>

					<div class="space-y-4">
						<div class="flex items-center justify-between py-2">
							<div>
								<div class="font-medium">系统通知</div>
								<div class="text-muted text-sm">接收系统维护、更新等通知</div>
							</div>
							<UCheckbox v-model="state.systemNotice" aria-label="系统通知" />
						</div>

						<div class="flex items-center justify-between py-2">
							<div>
								<div class="font-medium">安全通知</div>
								<div class="text-muted text-sm">
									接收登录异常、密码修改等安全通知
								</div>
							</div>
							<UCheckbox v-model="state.securityNotice" aria-label="安全通知" />
						</div>

						<div class="flex items-center justify-between py-2">
							<div>
								<div class="font-medium">邮件通知</div>
								<div class="text-muted text-sm">通过邮件接收重要通知</div>
							</div>
							<UCheckbox v-model="state.emailNotice" aria-label="邮件通知" />
						</div>
					</div>
				</UCard>
			</div>
		</template>
	</UDashboardPanel>
</template>
