import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";
import { ObjectId } from "bson";

// _id
// userId user unique id
// uid  word unique id
// createdAt
// updatedAt

export type TWord = {
    uid: string
    word: string
    type: "word" | "phrase" | "sentence"
    shortMeaning?: string
    examples?: string
    isFavorite?: boolean
    proficiencyLevel: string
    viewCount: number
    lastViewedDaysAgo: number
    createdAt: string | Date
    updatedAt: string | Date
    syncStatus: string
    retryCount?: number
    details?: string
    lastSyncAttempt?: string | Date
    categories?: any[]
    tags?: any[]
    attachments?: any[]
    comments?: any[]
}

export function getAuthId(req: any) {
    console.log(req?.user)
    let authId = req?.user?.userId
    if (!authId) {
        authId = ""
    } else {
        authId = new ObjectId(authId)
    }
    return authId
}

class WordController {

    async getWords(req: Request, res: Response) {
        const items = await RepositoryBase.findAll("word", {})
        res.status(200).json({
            data: items
        })
    }

    async updateWord(req: Request, res: Response) {
        const {
            word,
            type,
            userId,
            shortMeaning,
            details,
            isFavorite,
            viewCount,
            lastViewedDaysAgo,
            createdAt,
            updatedAt,
            syncStatus,
            retryCount,
            lastSyncAttempt,
            categories,
            tags,
            attachments,
            comments,
        } = req.body

        console.log("Updating word:", req.body)

        if (!word) {
            throw Error("Word required")
        }

        const uid = req.params.uid

        await RepositoryBase.updateOneWithUpsert("word", {
            uid: uid
        }, {
            word,
            type,
            userId,
            shortMeaning,
            details,
            isFavorite,
            viewCount,
            lastViewedDaysAgo,
            retryCount,
            lastSyncAttempt,
            categories: categories || [],
            tags: tags || [],
            attachments: attachments || [],
            comments: comments || [],
            syncStatus: "synced",
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated" })
    }

    async wordPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const limit = 50
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("word", query, {
            sort: { updatedAt: 1 },
            limit: limit
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new WordController())
