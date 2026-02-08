import RepositoryBase from "../respository/repository.base";
import { Request, Response } from "express"
import date from "../utils/date";

class TagController {

    async getTags(req: Request, res: Response) {
        const items = await RepositoryBase.findAll("tag", {})
        res.status(200).json({
            data: items
        })
    }

    async updateTag(req: Request, res: Response) {
        const {
            name,
            createdAt,
            updatedAt,
            syncStatus,
            isDeleted
        } = req.body

        const uid = req.params.uid

        await RepositoryBase.updateOneWithUpsert("tag", {
            uid: uid
        }, {
            name,
            syncStatus: "synced",
            isDeleted: !!isDeleted,
            updatedAt: Number(updatedAt) || date.now(),
            createdAt: Number(createdAt) || date.now(),
        })

        res.status(200).json({ message: "Successfully updated" })
    }

    async tagPull(req: Request, res: Response) {
        const since = Number(req.query?.since) || 0
        const limit = 50
        const query = { updatedAt: { $gt: Number(since) } };
        const items = await RepositoryBase.findAll("tag", query, {
            sort: { updatedAt: 1 },
            limit: limit
        })

        res.json({
            data: items,
            hasMore: items?.length > 0
        });
    }
}

export default (new TagController())
