
export type TSocialUser = {
    phone?: string
    providerId: string
    email?: string
    login: string
    emailVerified?: boolean
    phoneVerified?: boolean
    providerData: Record<string, any>
    fullName: string
    avatar: string
    provider: "slack" | "google" | "github" | "apple" | "microsoft" | "discord" | "twitter" | "facebook" | "linkedin"
    redirectUrl: string
    accountStatus?: "approved" | "pending" | "rejected"
}
