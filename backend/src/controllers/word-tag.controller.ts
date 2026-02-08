import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";

class WordTagController {
    async updateWordTag(req: Request, res: Response) {
        const {
            postId,
            tagId,
            syncStatus,
            createdAt,
            updatedAt
        } = req.body

        const uid = req.params.uid

        await RepositoryBase.updateOneWithUpsert("post_tags", {
            uid: uid
        }, {
            postId,
            tagId,
            syncStatus: "synced",
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated word-tag association" })
    }

    async wordTagPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("post_tags", query, {
            sort: { updatedAt: 1 }
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new WordTagController())
