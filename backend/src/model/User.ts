import {BaseModelInterface} from "./BaseModel";

export interface TUser {
    _id?: string
    provider?:  "slack" | "google" | "github" | "apple" | "microsoft" | "discord" | "twitter" | "facebook" | "linkedin" | "local"
    providerId?: string;
    username?: string;
    fullName: string;
    email: string;
    password?: string;
    avatar?: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;
}

class User extends BaseModelInterface implements TUser {
    static collectionName = "user"
    _id?: string
    username?: string;
    provider?: TUser["provider"]
    providerId?: string;
    fullName: string;
    email: string;
    password?: string;
    avatar?: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;

    constructor(d: TUser) {
        super()
        this.provider = d.provider
        this.providerId = d.providerId || ""
        this.username = d.username
        this.fullName = d.fullName
        this.email = d.email
        this.password = d.password
        this.avatar = d.avatar
        this.createdAt = d.createdAt
        this.updatedAt = d.updatedAt
    }

    static indexed() {
        return [
            {idxSpec: {email: 1}, idxOpt: {unique: true}},
            {idxSpec: {provider: 1}},
            {idxSpec: {createdAt: -1}},
        ]
    }
}

export default User
