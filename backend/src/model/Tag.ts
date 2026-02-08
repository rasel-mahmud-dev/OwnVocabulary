import {BaseModelInterface} from "./BaseModel";

class Tag extends BaseModelInterface {
    static collectionName = "tags"
    tag: string
    _id?: string
    createdAt: Date
    updatedAt: Date

    constructor(tag: string) {
        super()
        this.tag = tag
        this.createdAt = new Date()
        this.updatedAt = new Date()
    }

    static indexed() {
        return [
            {idxSpec: {tag: 1}, idxOpt: {unique: true}},
        ]
    }
}

export default Tag
