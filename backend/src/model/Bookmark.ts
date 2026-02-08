import {slugify} from "../utils/slugify";

export interface TBookmark {
    _id?: string
    title: string;
    slug: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;
}

class Bookmark implements TBookmark {
    static collectionName = "bookmark"
    _id?: string
    title: string;
    slug: string;
    updatedAt: string | undefined | Date;
    createdAt: string | undefined | Date;

    constructor(d: TBookmark) {
        this.title = d.title
        this.slug = slugify(d.title) || d.title
        this.createdAt = d.createdAt
        this.updatedAt = d.updatedAt
    }

    static indexed() {
        return [
            {idxSpec: {slug: 1}, idxOpt: {unique: true}},
        ]
    }
}

// attended / standup
// laptop managmwne
// profile
// cuti

// ADMIN -> attendance
// ADMIN -> laptop
// ADMIN -> profile

export default Bookmark
