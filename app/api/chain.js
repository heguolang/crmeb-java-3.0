import request from "@/utils/request.js";

export function chainMyIdentity() {
	return request.get("chain/my-identity");
}

export function chainMyTeam(depth = 3) {
	return request.get("chain/my-team", { depth });
}

export function chainCommissionList(data) {
	return request.get("chain/commission-list", data);
}

export function chainCommissionSummary() {
	return request.get("chain/commission-summary");
}

export function chainBindParent(data) {
	return request.post("chain/bind-parent", data);
}

export function chainPromotePoster() {
	return request.get("chain/promote-poster");
}

export function chainRules() {
	return request.get("chain/rules");
}

export function chainNotifyList(data) {
	return request.get("chain/notify-list", data);
}

export function chainNotifyUnread() {
	return request.get("chain/notify-unread");
}

export function chainNotifyRead(id) {
	return request.post("chain/notify-read?id=" + id);
}
