/*
 * SPDX-License-Identifier: AGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2026 Bharat Dev Burman
 */

package dev.crazo7924.onlymusic.data.repository

interface MusicRepository :
    MediaRepository,
    SearchRepository,
    SearchHistoryRepository,
    RecentsRepository,
    QueueRepository
